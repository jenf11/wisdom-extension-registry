/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package registry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import javassist.util.proxy.Proxy;
import org.apache.commons.io.IOUtils;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.security.Authenticated;
import org.wisdom.api.templates.Template;
import org.wisdom.monitor.service.MonitorExtension;
import org.wisdom.orientdb.object.OrientDbCrud;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * A controller to manage the list of extensions available for the Wisdom framework.
 * The list is saved in memory using the Wisdom-OrientDB extension. Database is configured in the
 * application.conf file. Some methods have authorisation limited to Monitor authentication.
 */

@Controller

public class ExtensionRegistryController extends DefaultController implements MonitorExtension {

    @View("registry/managerView")
    Template manage;
    @View("registry/userView")
    Template user;
    @View("registry/developerView")
    Template dev;

    @Model(value = Extension.class)
    private OrientDbCrud<Extension, String> extensionCrud;

    Class klass = Proxy.class;

    @Requires
    Json json;


    /**
     * The action method returning the user's view of the extension page. It handles
     * HTTP GET request on the "/user" URL.
     *
     * @return the user extension page.
     */

    @Route(method = HttpMethod.GET, uri = "/registry")
    public Result user() {
        return ok(render(user));
    }

    /**
     * The action method returning the developer's view of the extension page. It handles
     * HTTP GET request on the "/dev" URL.
     * TODO: Separate authorisation
     * @return the developer extension page.
     */
    @Route(method = HttpMethod.GET, uri = "/registry/dev")
    public Result dev() {
        return ok(render(dev));
    }

    /**
     * The action method returning the administration's view of the extension page. It handles
     * HTTP GET request on the "/monitor/manage" URL.  It is accessed via the Wisdom Monitor.
     *
     * @return the managed extension page.
     */
    @Authenticated("Monitor-Authenticator")
    @Route(method = HttpMethod.GET, uri = "/monitor/registry/manage")
    public Result manage() {
        return ok(render(manage));
    }

    /**
     * The action method returning the user's view of the extension page. It handles
     * HTTP GET request on the "/list" URL.
     *
     * @return extension list in a json structure.
     */
    @Route(method = HttpMethod.GET, uri = "/registry/list")
    public Result get() {

        List<Extension> list = new LinkedList<Extension>();
        for (Extension extension : extensionCrud.findAll()) {
            list.add(extension);
        }
        return ok(list).json();
    }

    /**
     * The action method deleting a specific extension from the database. It handles
     * HTTP DELETE request on the "/list" URL.  Must be authenticated via the Wisdom Monitor
     * for this method.
     *
     * @return result.
     */
    @Authenticated("Monitor-Authenticator")
    @Route(method = HttpMethod.DELETE, uri = "/registry/list/{id}")
    public Result delete(@Parameter("id") String id) {
        removeExtensionById(id);
        return ok();
    }

    /**
     * The action method deleting a specific extension from the database. It handles
     * HTTP DELETE request on the "/list" URL.  Must be authenticated via the Wisdom Monitor
     * for this method.
     *
     * @return result.
     */
    @Authenticated("Monitor-Authenticator")
    @Route(method = HttpMethod.POST, uri = "/registry/list/{id}")
    public Result update(@Parameter("id") String id) {
        String url = extensionCrud.findOne(id).getSelf();
        return ok(parseUrl(url));
    }

    /**
     * The action method that loads a json object via a url and adds it to the database. It handles
     * HTTP POST request on the "/upload" URL.
     *
     * @return json structure containing the new extension
     */
    @Route(method = HttpMethod.POST, uri = "/registry/upload")
    public Result addExt(final @FormParameter("url") String u) {
               return ok(parseUrl(u));
    }

    /**
     * A method that parses a url expecting a json object.
     * @param url the url to be parsed.
     * @return  the json object if everything went well or a json object with an error msg if it
     * didn't.
     */
    private Result parseUrl(final String url){
        return async(new Callable<Result>() {
            @Override
            public Result call() throws Exception {

                try {
                    URL u = new URL(url);
                    String j = IOUtils.toString(u);
                    JsonNode node = json.parse(j);
                    node = createExt((ObjectNode) node, url);
                    return ok(node);
                } catch (IOException e) {
                    // Cannot read the URL
                    logger().error("File not found or is unable to be read " + e);

                    return ok(json.newObject().put("error", "An error has occurred:").put("reason",
                            "File not found or unable to be read."));
                } catch (Exception e) {
                    logger().error(" A problem has occurred please check that your file is " +
                            "valid JSON. " +
                            " - " + e.getMessage() +
                            " (" + e.getClass()
                            .getName() + ")", e);
                    return ok(json.newObject().put("error", "An error has occurred:").put("reason",
                            "Please check that your file is " +
                                    "valid JSON. "));
                }
            }
        });
    }

    /**
     * Method that reads a json structure validating that it has all of the required javabean
     * fields that cannot be null by using Validator. If all goes well the object is added to the
     * database or updated if it already exists.
     *
     * @return json structure containing either the extension or error message
     */
    @Requires
    Validator validator;
    private JsonNode createExt(ObjectNode node, String url) {
        ObjectNode result = json.newObject();
        //map our incoming json to the
        Extension extension = json.mapper().convertValue(node, Extension.class);
        Set<ConstraintViolation<Extension>> errors = validator.validate(extension);
        if (!errors.isEmpty()) {
            Iterator<ConstraintViolation<Extension>> i = errors.iterator();
            String msg = "";
            while (i.hasNext()) {
                msg = msg + i.next().getPropertyPath() + " ";

            }
            result.put("error", "Error while adding extension").put("reason",
                    msg + "need(s) to be set");
            logger().error("Something really bad happened...");
            logger().error(errors.toString());
            return result;
        }


            List<Extension> omg = extensionCrud.query(new OSQLSynchQuery<Extension>("select * " +
                    "from  " +
                    "Extension where name like '"+extension.getName()+"'"));



        if(!omg.isEmpty()){
           updateExtInDB(extension, url, omg.get(0));
            node.put("updated", true);
        }
        else {
            addExtToDB(extension, url);
            node.put("updated", false);
        }
        return node;
    }

    private void addExtToDB(Extension extension, String url){
        extension.setDateAdded(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        extension.setSelf(url);
        extensionCrud.save(extension);

    }
    private void updateExtInDB(Extension extension, String url, Extension oldExt){
       extension.setDateAdded(oldExt.getDateAdded());
        extension.setDateUpdated(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        extension.setSelf(url);
        removeExtensionById(oldExt.getId());
        extensionCrud.save(extension);

    }



    /**
     * Deletes the extension specified by database id from the database.
     * @param id is the id field in the database for the extension.
     */
    private void removeExtensionById(String id) {
        extensionCrud.delete(id);
    }

    /**
     * Method required from implementing MonitorExtension. Displays the label in the Wisdom
     * Monitor side bar.
     * @return  the name of the label.
     */
    @Override
    public String label() {
        return "Extension Manager";
    }

    /**
     * Method required from implementing MonitorExtension.
     * @return  url of the method to be accessed via the Monitor.
     */
    @Override
    public String url() {
        return "/monitor/registry/manage";
    }

    /**
     * Method required from implementing MonitorExtension. Displays the category in the Wisdom
     * Monitor side bar.
     * @return  the name of the category.
     */
    @Override
    public String category() {
        return "Documentation";
    }
}

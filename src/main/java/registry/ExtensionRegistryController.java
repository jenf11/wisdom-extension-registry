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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Your first Wisdom Controller.
 */
@Controller
public class ExtensionRegistryController extends DefaultController {
     private Map <String, Extension> list;
    /**
     * Injects a template named 'welcome'.
     */
    @View("welcome")
    Template welcome;

    @View("managerView")
    Template manage;
    @View("userView")
    Template user;
    @View("developerView")
    Template dev;


    @Requires
    Json json;

    public ExtensionRegistryController() {
        createFakeList();
    }

    public void createFakeList(){
        this.list = new HashMap<String, Extension>();
        String count = String.valueOf(list.size()+1);

            list.put("tracer",new Extension());
        list.put("javaBob",new Extension());
        list.put("cssstuff",new Extension());
        list.put("Imaketheworld",new Extension());
        list.get("javaBob").setLicense(new License("MIT","https://raw.github.com/jashkenas/underscore/master/LICENSE"));
        list.get("javaBob").setKeyWords(new String[]{"stuff","things","java"});
        list.get("javaBob").setRepository(new Repository("git","http://somewhere"));
        list.get("javaBob").setHomepage("http://somewhere");
        String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        list.get("javaBob").setDate(date);
        list.get("Imaketheworld").setDate(date);
        list.get("cssstuff").setDate(date);
        list.get("tracer").setDate(date);


    }


    /**
     * The action method returning the welcome page. It handles
     * HTTP GET request on the "/" URL.
     *
     * @return the welcome page
     */
    @Route(method = HttpMethod.GET, uri = "/")
    public Result welcome() {
        return ok(render(welcome, "welcome", "Welcome to Wisdom Framework!"));
    }

    @Route(method = HttpMethod.GET, uri = "/user")
    public Result user() {
        return ok(render(user));
    }

    @Route(method = HttpMethod.GET, uri = "/dev")
    public Result dev() {
        return ok(render(dev));
    }

    @Route(method = HttpMethod.GET, uri = "/manage")
    public Result manage() {
        return ok(render(manage));
    }

    @Route(method = HttpMethod.GET, uri = "/list")
    public Result get() {
        return  ok(list).json();
    }


    @Route(method = HttpMethod.DELETE, uri = "/list/{id}")
    public Result delete(@Parameter("id") String id) {
        removeExtensionById(id);
        return ok();
    }

    @Route(method = HttpMethod.POST, uri = "/upload")
    public Result findExt(final @FormParameter("url") String u) {

        return async(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                try {
                    URL url = new URL(u);
                    String j = IOUtils.toString(url);
                    JsonNode node = json.parse(j);
                    node = addToList(node);
                    return ok(node);
                } catch (IOException e) {
                    // Cannot read the URL
                    logger().error("File not found or is unable to be read " + e );

                    return ok(json.newObject().put("error","An error has occurred:").put("reason",
                            "File not found or unable to be read."));
                } catch (Exception e) {
                    logger().error(" A problem has occurred please check that your file is " +
                            "valid JSON. " +
                            " - " + e.getMessage() +
                            " (" + e.getClass()
                            .getName() + ")");
                    return ok(json.newObject().put("error","An error has occurred:").put("reason",
                            "Please check that your file is " +
                            "valid JSON. " ));
                }
            }
        });


    }

    @Requires
    Validator validator;

    private JsonNode addToList(JsonNode node){
        ObjectNode result =  json.newObject();
        Extension extension = json.mapper().convertValue(node, Extension.class);
        Set<ConstraintViolation<Extension>> errors = validator.validate(extension);
        if (! errors.isEmpty()) {
            Iterator<ConstraintViolation<Extension>> i = errors.iterator();
            String msg = "";
             while(i.hasNext()){
                 msg = msg  + i.next().getPropertyPath()+ " ";

             }
            result.put("error", "Error while adding extension").put("reason",
                    msg + "need(s) to be set");
            logger().error("Something really bad happened...");
            logger().error(errors.toString());
            return result;
        }

        extension.setDate(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        list.put(extension.getName(), extension);
        return node;
    }
    private void removeExtensionById(String id) {
        list.remove(id);
    }

}

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

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;

import java.util.HashMap;
import java.util.Map;

/**
 * Your first Wisdom Controller.
 */
@Controller
public class ExtensionRegistryController extends DefaultController {
     private Map <String,ExtensionObj> list;
    /**
     * Injects a template named 'welcome'.
     */
    @View("welcome")
    Template welcome;

    @View("managerview")
    Template manage;
    @View("usersview")
    Template user;
    @View("developerview")
    Template dev;



    public void createFakeList(){
        //how can I make count a function? so if i use the variable count it recalates each time
        // the number?
        this.list = new HashMap<String, ExtensionObj>();
        String count = String.valueOf(list.size()+1);
        list.put(String.valueOf(list.size()+1),new ExtensionObj("Bob","I do stuff","url//","1.0","stuff//",
                new String[]{"Awesome"}));
        list.put(String.valueOf(list.size()+1),new ExtensionObj("Rob","I do stuff","url//","1.0","stuff//",
                new String[]{"Awesome"}));
        list.put(String.valueOf(list.size()+2),new ExtensionObj("Changer","I do more stuff",
                "url//","3.0","stuff//",
                new String[]{"Awesome"}));

    }

    public void addToList(Json extension){

    }
    public void deleteFromList(Json extension){

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
    @Route(method = HttpMethod.GET, uri = "/admin")
    public Result manage() {
        createFakeList();
        String n = this.list.get("1").exName;
        return ok(render(manage, "blob",list));
    }

    @Route(method = HttpMethod.GET, uri = "/list")
    public Result get() {
        createFakeList();
        return  ok(list).json();
    }


}

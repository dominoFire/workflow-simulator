package org.perez.workflow.elements;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by microkid on 8/08/16.
 */
public class WorkflowDeserializer
    implements JsonDeserializer<Workflow>
{
    @Override
    public Workflow deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx)
            throws JsonParseException
    {
        if(type.equals(Workflow.class)) {
            JsonObject root = jsonElement.getAsJsonObject();
            Workflow w = new Workflow();
            Map<String, Task> tasks = new HashMap<>();

            JsonArray jsonTasks = root.get("tasks").getAsJsonArray();
            for(JsonElement jt: jsonTasks) {
                Task t = ctx.deserialize(jt, Task.class);
                t.setDependencies(new HashSet<>());
                t.setSuccessors(new HashSet<>());
                w.addTask(t);
                tasks.put(t.getName(), t);
            }

            JsonArray jsonDependencies = root.get("dependencies").getAsJsonArray();
            for(JsonElement je: jsonDependencies) {
                Task t1 = ctx.deserialize(je.getAsJsonObject().get("_1"), Task.class);
                Task t2 = ctx.deserialize(je.getAsJsonObject().get("_2"), Task.class);
                w.addDependency(tasks.get(t1.getName()), tasks.get(t2.getName()));
            }

            return w;
        }
        else {
            throw new JsonParseException("No es Workflow");
        }
    }
}

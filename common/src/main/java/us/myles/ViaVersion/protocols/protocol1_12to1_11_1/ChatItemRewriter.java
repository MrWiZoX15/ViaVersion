package us.myles.ViaVersion.protocols.protocol1_12to1_11_1;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import us.myles.ViaVersion.api.data.UserConnection;

import java.util.regex.Pattern;

public class ChatItemRewriter {
    private static final Pattern indexRemoval = Pattern.compile("\\d+:(?=([^\"\\\\]*(\\\\.|\"([^\"\\\\]*\\\\.)*[^\"\\\\]*\"))*[^\"]*$)");
    // Taken from https://stackoverflow.com/questions/6462578/alternative-to-regex-match-all-instances-not-inside-quotes

    public static void toClient(JsonElement element, UserConnection user) {
        if (element instanceof JsonObject) {
            JsonObject obj = (JsonObject) element;
            if (obj.has("hoverEvent")) {
                if (obj.get("hoverEvent") instanceof JsonObject) {
                    JsonObject hoverEvent = (JsonObject) obj.get("hoverEvent");
                    if (hoverEvent.has("action") && hoverEvent.has("value")) {
                        String type = hoverEvent.get("action").getAsString();
                        if (type.equals("show_item") || type.equals("show_entity")) {
                            JsonElement value = hoverEvent.get("value");

                            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                                String newValue = indexRemoval.matcher(value.getAsString()).replaceAll("");
                                hoverEvent.addProperty("value", newValue);
                            } else if (value.isJsonArray()) {
                                JsonArray newArray = new JsonArray();

                                for (JsonElement valueElement : value.getAsJsonArray()) {
                                    if (valueElement.isJsonPrimitive() && valueElement.getAsJsonPrimitive().isString()) {
                                        String newValue = indexRemoval.matcher(valueElement.getAsString()).replaceAll("");
                                        newArray.add(new JsonPrimitive(newValue));
                                    }
                                }

                                hoverEvent.add("value", newArray);
                            }
                        }
                    }
                }
            } else if (obj.has("extra")) {
                toClient(obj.get("extra"), user);
            }
        } else if (element instanceof JsonArray) {
            JsonArray array = (JsonArray) element;
            for (JsonElement value : array) {
                toClient(value, user);
            }
        }
    }
}

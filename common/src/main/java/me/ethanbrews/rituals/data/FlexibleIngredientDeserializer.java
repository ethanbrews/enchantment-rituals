package me.ethanbrews.rituals.data;


import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FlexibleIngredientDeserializer implements JsonDeserializer<String[][]> {

    @Override
    public String[][] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        if (!json.isJsonArray()) {
            throw new JsonParseException("Ingredients must be an array");
        }

        JsonArray outerArray = json.getAsJsonArray();
        List<String[]> result = new ArrayList<>();

        for (JsonElement element : outerArray) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                // Single string - wrap in array
                result.add(new String[]{element.getAsString()});

            } else if (element.isJsonArray()) {
                // Array of strings
                JsonArray innerArray = element.getAsJsonArray();
                String[] innerStrings = new String[innerArray.size()];

                for (int i = 0; i < innerArray.size(); i++) {
                    JsonElement innerElement = innerArray.get(i);
                    if (!innerElement.isJsonPrimitive() || !innerElement.getAsJsonPrimitive().isString()) {
                        throw new JsonParseException("All inner elements must be strings");
                    }
                    innerStrings[i] = innerElement.getAsString();
                }
                result.add(innerStrings);

            } else {
                throw new JsonParseException("Each ingredient must be a string or array of strings");
            }
        }

        return result.toArray(new String[0][]);
    }
}

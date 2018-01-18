package ru.cyberspacelabs.nocounterfeit.json;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Created by mike on 14.04.16.
 */
public class LongIntegerJSONAdapter extends TypeAdapter<Number> {
	@Override
	public void write(JsonWriter out, Number value) throws IOException {
		out.value(value);
	}

	@Override
	public Number read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return 0;
		}
		try {
			String result = in.nextString();
			if ("".equals(result)) {
				return 0;
			}
			return Long.parseLong(result);
		} catch (NumberFormatException e) {
			throw new JsonSyntaxException(e);
		}
	}
}

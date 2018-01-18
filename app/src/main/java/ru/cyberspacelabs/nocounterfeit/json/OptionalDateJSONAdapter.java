package ru.cyberspacelabs.nocounterfeit.json;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mike on 27.04.16.
 */
public class OptionalDateJSONAdapter extends TypeAdapter<Date> {
	private SimpleDateFormat sdfout = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public void write(JsonWriter out, Date value) throws IOException {
		out.value(sdfout.format(value));
	}

	@Override
	public Date read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return new Date(0);
		}
		try {
			String result = in.nextString();
			if ("".equals(result)) {
				return new Date(0);
			}
			return sdfout.parse(result);
		} catch (Exception e) {
			throw new JsonSyntaxException(e);
		}
	}
}

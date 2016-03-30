/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.mobile.sdk;

import com.liferay.mobile.sdk.http.Headers;
import com.liferay.mobile.sdk.http.Headers.ContentType;
import com.liferay.mobile.sdk.http.Request;
import com.liferay.mobile.sdk.http.Response;
import com.liferay.mobile.sdk.json.JSONParser;
import com.liferay.mobile.sdk.v2.HttpClient;
import com.liferay.mobile.sdk.v2.OkHttpClientImpl;

import java.lang.reflect.Type;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Bruno Farache
 */
public class Call<T> {

	public static void cancel(Object call) {
		client.cancel(call);
	}

	public static HttpClient client() {
		return client;
	}

	public Call(Object body, Type type) {
		this(body, type, ContentType.JSON);
	}

	public Call(Object body, Type type, ContentType contentType) {
		this.body = body;
		this.type = type;
		this.contentType = contentType;
	}

	public void async(Callback<T> callback) {
		async(Config.global(), callback);
	}

	public void async(Config config, Callback<T> callback) {
		callback.type(this.type);
		Request request = request(config);
		client.async(request, callback);
	}

	public Object body() {
		return body;
	}

	public T execute() throws Exception {
		return execute(Config.global());
	}

	public T execute(Config config) throws Exception {
		Request request = request(config);
		Response response = client.sync(request);
		return JSONParser.fromJson(response, type);
	}

	protected static JSONArray bodies(Call[] calls) {
		JSONArray commands = new JSONArray();

		for (Call call : calls) {
			commands.put(call.body());
		}

		return commands;
	}

	protected Request request(Config config) {
		Map<String, String> headers = config.headers();
		headers.put(Headers.CONTENT_TYPE, contentType.value);
		String path = "/invoke";

		if (contentType == ContentType.JSON) {
			body = body.toString();
		}
		else if (contentType == ContentType.MULTIPART) {
			JSONObject jsonObject = (JSONObject)body;
			path = (String)jsonObject.keys().next();
			body = jsonObject.optJSONObject(path);
		}

		return Request.url(config.url() + path)
			.config(config)
			.headers(headers)
			.body(body)
			.tag(this);
	}

	protected static HttpClient client = new OkHttpClientImpl();

	protected Object body;
	protected ContentType contentType;
	protected Type type;

}
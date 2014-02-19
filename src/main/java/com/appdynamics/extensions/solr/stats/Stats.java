/**
 * Copyright 2013 AppDynamics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.solr.stats;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.singularity.ee.util.httpclient.HttpClientWrapper;
import com.singularity.ee.util.httpclient.HttpExecutionRequest;
import com.singularity.ee.util.httpclient.HttpExecutionResponse;
import com.singularity.ee.util.httpclient.HttpOperation;
import com.singularity.ee.util.httpclient.IHttpClientWrapper;
import com.singularity.ee.util.log4j.Log4JLogger;

public abstract class Stats {

	private static Logger logger = Logger.getLogger(Stats.class.getName());

	public String url;

	private String resourceAppender = "/solr/admin/mbeans";

	private final String queryString = "?stats=true&wt=json";

	public Stats(final String host, final String port) {
		this.url = "http://" + host + ":" + port;
		logger.setLevel(Level.INFO);
	}

	public String getJsonResponseString(String resource) {
		IHttpClientWrapper httpClient = HttpClientWrapper.getInstance();
		HttpExecutionRequest request = new HttpExecutionRequest(resource, "", HttpOperation.GET);
		HttpExecutionResponse response = httpClient.executeHttpOperation(request, new Log4JLogger(logger));
		if (response.isExceptionHappened() || response.getStatusCode() == 400) {
			logger.error("Solr instance down OR URL " + resource + " not supported");
			throw new RuntimeException("Solr instance down OR URL " + resource + " not supported");
		}
		return response.getResponseBody();
	}

	public abstract void populateStats();

	public String getUrl() {
		return url;
	}

	public void setUrl(String host, String port) {
		this.url = "http://" + host + ":" + port;
	}

	public String getQueryString() {
		return queryString;
	}

	public String getResourceAppender() {
		return resourceAppender;
	}

	public void setResourceAppender(String resourceAppender) {
		this.resourceAppender = resourceAppender;
	}

}
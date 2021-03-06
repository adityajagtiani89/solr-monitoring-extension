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

package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.http.Response;
import com.appdynamics.extensions.http.SimpleHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SolrHelper {

    private static final double BYTES_CONVERSION_FACTOR = 1024.0;

    private static Logger logger = Logger.getLogger(SolrHelper.class);

    private SimpleHttpClient httpClient;

    public SolrHelper(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public static JsonNode getJsonNode(Response response) throws IOException {
        if (response == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response.string(), JsonNode.class);
    }

    /**
     * Converts Bytes to MegaBytes
     *
     * @param d
     * @return
     */
    public static double convertBytesToMB(Double d) {
        if (d != null) {
            d = d / (BYTES_CONVERSION_FACTOR * BYTES_CONVERSION_FACTOR);
        }
        return d;
    }

    /**
     * Converts from String form with Units("224 MB") to a number(224)
     *
     * @param valueStr
     * @return
     */
    public static Double convertMemoryStringToDouble(String valueStr) {
        if (!Strings.isNullOrEmpty(valueStr)) {
            String strippedValueStr = null;
            try {
                if (valueStr.contains("KB")) {
                    strippedValueStr = valueStr.split("KB")[0].trim();
                    return unLocalizeStrValue(strippedValueStr) / BYTES_CONVERSION_FACTOR;
                } else if (valueStr.contains("MB")) {
                    strippedValueStr = valueStr.split("MB")[0].trim();
                    return unLocalizeStrValue(strippedValueStr);
                } else if (valueStr.contains("GB")) {
                    strippedValueStr = valueStr.split("GB")[0].trim();
                    return unLocalizeStrValue(strippedValueStr) * BYTES_CONVERSION_FACTOR;
                }
            } catch (Exception e) {
                // ignore
            }
            logger.error("Unrecognized string format: " + valueStr);
        }
        return null;
    }

    private static Double unLocalizeStrValue(String valueStr) {
        try {
            Locale loc = Locale.getDefault();
            return Double.valueOf(NumberFormat.getInstance(loc).parse(valueStr).doubleValue());
        } catch (ParseException e) {
            logger.error("Exception while unlocalizing number string " + valueStr, e);
        }
        return null;
    }

    public static Double multipyBy(Double value, int multiplier) {
        if (value != null) {
            value = value * multiplier;
        }
        return value;
    }

    /**
     * Fetches the solr-mbeans node from JsonResponse and puts it into a map
     * with key as Category name and its values as JsonNode
     *
     * @param mbeansUri
     * @return
     * @throws IOException
     */
    public Map<String, JsonNode> getSolrMBeansHandlersMap(String core, String mbeansUri) {
        String uri = String.format(mbeansUri, core);
        Response response = null;
        Map<String, JsonNode> solrStatsMap = new HashMap<String, JsonNode>();
        try {
            response = httpClient.target().path(uri).get();
            JsonNode jsonNode = getJsonNode(response);
            if (jsonNode != null) {
                JsonNode solrMBeansNode = jsonNode.path("solr-mbeans");
                if (solrMBeansNode.isMissingNode()) {
                    throw new IllegalArgumentException("Missing node while parsing solr-mbeans node json string for " + core + uri);
                }
                for (int i = 1; i <= solrMBeansNode.size(); i += 2) {
                    solrStatsMap.put(solrMBeansNode.get(i - 1).asText(), solrMBeansNode.get(i));
                }
            }
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        } finally {
            closeResponse(response);
        }
        return solrStatsMap;
    }

    public List<String> getCores(String uri) {
        List<String> cores = new ArrayList<String>();
        Response response = null;
        try {
            response = httpClient.target().path(uri).get();
            JsonNode node = getJsonNode(response);
            if (node != null) {
                Iterator<String> fieldNames = node.path("status").fieldNames();
                while (fieldNames.hasNext()) {
                    cores.add(fieldNames.next());
                }
                if (cores.isEmpty()) {
                    logger.error("There are no SolrCores running. Using this Solr Extension requires at least one SolrCore.");
                    throw new RuntimeException();
                }
                if (logger.isDebugEnabled())
                    logger.debug("Cores / Collections size is " + cores.size());
            }
        } catch (Exception e) {
            logger.error("Error while fetching cores " + uri, e);
            throw new RuntimeException();
        } finally {
            closeResponse(response);
        }
        return cores;
    }

    public String getDefaultCore(String uri) {
        String defaultCore = "";
        Response response = null;
        try {
            response = httpClient.target().path(uri).get();
            JsonNode node = getJsonNode(response);
            if (node != null) {
                defaultCore = node.path("defaultCoreName").asText();
                if (logger.isDebugEnabled())
                    logger.debug("Default Core name is " + defaultCore);
            }
        } catch (Exception e) {
            logger.error("Error while fetching default Core name " + uri, e);
            throw new RuntimeException();
        } finally {
            closeResponse(response);
        }
        return defaultCore;
    }

    public boolean checkIfMBeanHandlerSupported(String resource) throws IOException {
        Response response = null;
        try {
            response = httpClient.target().path(resource).get();
            JsonNode jsonNode = getJsonNode(response);
            if (jsonNode != null) {
                JsonNode node = jsonNode.findValue("QUERYHANDLER");
                if (node == null) {
                    logger.error("Missing 'QUERYHANDLER' when checking for mbeans " + resource);
                    return false;
                }
                boolean mbeanSupport = node.has("/admin/mbeans");
                if (!mbeanSupport) {
                    logger.error("Stats are collected through an HTTP Request to SolrInfoMBeanHandler");
                    logger.error("SolrInfoMbeanHandler (/admin/mbeans) or /admin request handler is disabled in solrconfig.xml " + resource);
                }
                return mbeanSupport;
            } else {
                logger.error("Response null when accessing " + resource);
                return false;
            }
        } catch (Exception e) {
            logger.error("Exception when mbean handler check " + resource, e);
            return false;
        } finally {
            closeResponse(response);
        }
    }

    public void closeResponse(Response response) {
        try {
            if (response != null) {
                response.close();
            }
        } catch (Exception e) {
            logger.error("Error while closing input stream", e);
        }
    }

    public SimpleHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }
}

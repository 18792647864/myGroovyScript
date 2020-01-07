
@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.6' )

import groovyx.net.http.HTTPBuilder
 
import groovyx.net.http.RESTClient
import groovyx.net.http.HttpResponseDecorator
 
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*



def http = new HTTPBuilder()

 
http.request( 'http://localhost:8081', GET, TEXT ) { 
    req ->
    uri.path = '/ticket/rules/list?board=1&tenantId=36'
    headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
    headers.Accept = 'application/json'
    response.success = { 
        resp, reader ->
        assert resp.statusLine.statusCode == 200
        println "Got response: ${resp.statusLine}"
        println "Content-Type: ${resp.headers.'Content-Type'}"
        println reader.text
    }
    response.'404' = {
        println 'Not found'
    }
}
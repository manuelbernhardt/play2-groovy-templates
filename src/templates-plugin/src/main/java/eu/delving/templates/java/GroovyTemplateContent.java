package eu.delving.templates.java;

import play.mvc.Content;

/**
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */
public class GroovyTemplateContent implements Content {

    private final String body;
    private final String contentType;

    public GroovyTemplateContent(String body, String contentType) {
        this.body = body;
        this.contentType = contentType;
    }

    @Override
    public String body() {
        return body;
    }

    @Override
    public String contentType() {
        return contentType;
    }
}
package org.wso2.gw.emulator.http.server.contexts;

import java.util.regex.Pattern;

/**
 * Created by dilshank on 12/17/15.
 */
public class UtilityContext {

    private Pattern pattern;



    public UtilityContext(){
        pattern = Pattern.compile("(\\@\\{[a-zA-Z]*.[a-zA-Z]*[0-9]*\\})");
    }

    public Pattern getPattern() {
        return pattern;
    }
}

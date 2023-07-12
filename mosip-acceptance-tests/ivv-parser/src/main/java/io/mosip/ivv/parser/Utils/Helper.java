package io.mosip.ivv.parser.Utils;

import io.mosip.ivv.core.dtos.IDObjectField;
import io.mosip.ivv.core.dtos.Person;
import io.mosip.ivv.core.utils.MailHelper;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static io.mosip.ivv.core.utils.Utils.regex;

public class Helper {
	private static final Logger logger = LoggerFactory.getLogger(Helper.class);

    public static String readFileAsString(String path, Charset encoding){
        byte[] encoded = new byte[0];
        File f = new File(path);
        if(!f.exists()) {
            return null;
        }
        try {
            encoded = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
        	logger.error(e.getMessage());
        }
        return new String(encoded, encoding);
    }

}

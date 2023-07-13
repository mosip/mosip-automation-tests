package io.mosip.ivv.parser.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

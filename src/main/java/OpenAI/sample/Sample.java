package OpenAI.sample;

import OpenAI.OpenAIUtil;
import io.github.sashirestela.openai.domain.image.Image;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

// test4
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


@Slf4j
public class Sample {

    private static String APIKey = "";
    private static String assistantId = "";
    private static String threadId = "";
    private static OpenAIUtil openAIUtil = new OpenAIUtil(APIKey, assistantId);

    public static void main(String[] args) {
        test1(false);
        test2(false);
    }

    private static void test1(boolean runCheck){
        if(!runCheck){return;}

        log.info("开始问 OpenAI");

        String openAI_result = openAIUtil.assistant(threadId, "你好");

        log.info(openAI_result);
    }

    private static String promptStr = "繪製一隻可愛小貓";
    private static void test2(boolean runCheck){
        if(!runCheck){return;}
        log.info("开始问 OpenAI");

        List<Image> result = openAIUtil.imageGeneration(promptStr);

        for (Image image : result) {
            log.info(image.toString());
        }
    }


}

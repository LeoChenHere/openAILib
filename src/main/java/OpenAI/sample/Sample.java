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
        test3(false);
        test4(true);
        test5(true);
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

    private static void test3(boolean runCheck){
        if(!runCheck){return;}
        log.info("測試日誌");
    }


    private static void test4(boolean runCheck){
        if(!runCheck){return;}
        log.info("测试: 检查目录");

        String folderPath = "./db";
        String fileNamePattern = "*.sqlite";
        File file = new File(folderPath);
        if (file.isDirectory()) {
            log.info("{} 目錄存在", folderPath);
            _test4_1(runCheck, folderPath, fileNamePattern);
        }else{
            log.info("{} 目錄不存在，建立目錄", folderPath);
            file.mkdir();
        }
    }


    private static void _test4_1(boolean runCheck, String folderPath, String fileNamePattern){
        if(!runCheck){return;}
        log.info("测试: 列出內容");

        log.info("列出指定目錄下副檔名({})的檔案: ", fileNamePattern);
        Path path = Path.of(folderPath);
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(path, fileNamePattern)){
            for (Path entry: stream){
                log.info("" + entry.getFileName());
            }
        }
        catch (IOException e){
            log.error("error while retrieving update configuration files " + e.getMessage());
        }
    }

    private static void test5(boolean runCheck){
        if(!runCheck){return;}

        log.info("测试 sqlite");

        Connection connection = null;
        Statement statement = null;
        try {
            // 加载SQLite驱动程序
            Class.forName("org.sqlite.JDBC");

            // 创建数据库连接
            connection = DriverManager.getConnection("jdbc:sqlite:./db/data.sqlite");

            // 创建Statement对象
            statement = connection.createStatement();

            // 创建表
            String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE, " +
                    "age INTEGER " +
                    ") "
                    ;
            statement.executeUpdate(createTableSQL);

            // 插入数据
            String insertSQL = "INSERT INTO users (name, age) VALUES ('John', 25)";
            statement.executeUpdate(insertSQL);

            log.info("数据插入成功！");
        } catch (org.sqlite.SQLiteException e) {
            // ref. https://www.sqlite.org/rescode.html#constraint
//            log.info("可能是重複了: \n{}:\n{}:\n{}", e.getMessage(), e.getErrorCode(), e.getResultCode().name());
//            e.printStackTrace();
            if( e.getErrorCode() == 19 ){
                log.info("數據重複:({}, {})", e.getErrorCode(), e.getResultCode().name());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 关闭Statement和Connection
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}

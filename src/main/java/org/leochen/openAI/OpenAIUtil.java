package org.leochen.openAI;

import org.leochen.openAI.consts.model;
import io.github.sashirestela.cleverclient.Event;
import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.common.content.ContentPart;
import io.github.sashirestela.openai.common.function.FunctionExecutor;
import io.github.sashirestela.openai.domain.assistant.*;
import io.github.sashirestela.openai.domain.assistant.events.EventName;
import io.github.sashirestela.openai.domain.image.Image;
import io.github.sashirestela.openai.domain.image.ImageRequest;
import io.github.sashirestela.openai.domain.image.ImageResponseFormat;
import io.github.sashirestela.openai.domain.image.Size;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Stream;

import static org.apache.logging.log4j.util.Strings.isEmpty;

@Slf4j
public class OpenAIUtil {
    private SimpleOpenAI openAI;

    private String assistantId;

    private String fileId;
    private FunctionExecutor functionExecutor;
    private String vectorStoreId;

    public OpenAIUtil(String openAIAPIKey, String assistantId) {
        this.openAI = SimpleOpenAI.builder().apiKey(openAIAPIKey).build();
        this.assistantId = assistantId;
        if(isEmpty(assistantId)){
            System.out.println("assistantId is null or empty");
        } // 沒有指定 assistantId 肯定是不行的，除非自己建一個 assistant

    }

    @Getter
    private String threadId; // 给予外层取得 threadId 的方法
    public String assistant(String threadId, String myMessage) { // threadId 有可能是 null
        log.info("原来的 threadId: " + threadId);

        var assistant = openAI.assistants().getOne(assistantId).join(); // 雖然好像沒用到 assistant 但還是要有這行
        var thread = (isEmpty(threadId)) ? openAI.threads().create().join() : openAI.threads().getOne(threadId).join();
        log.info("Thread id: " + thread.getId());
        threadId = thread.getId(); // 有可能有 threadId, 那就重複賦值；如果是新的 thread 那就透過這層賦值
        this.threadId = threadId; // 给予外层取得 threadId 的方法

        openAI.threadMessages()
                .create(threadId, ThreadMessageRequest.builder()
                        .role(ThreadMessageRole.USER)
                        .content(myMessage)
                        .build())
                .join();
        var runStream = openAI.threadRuns()
                .createStream(threadId, ThreadRunRequest.builder()
                        .assistantId(assistantId)
                        .build())
                .join();
        return _handleRunEvents(threadId, runStream);
    }

    private String _handleRunEvents(String threadId, Stream<Event> runStream) {
        StringBuffer sb = new StringBuffer();
        runStream.forEach(event -> {
            switch (event.getName()) {
                case EventName.THREAD_RUN_CREATED:
                case EventName.THREAD_RUN_COMPLETED:
                case EventName.THREAD_RUN_REQUIRES_ACTION:
                    var run = (ThreadRun) event.getData();
                    log.info("=====>> Thread Run: id=" + run.getId() + ", status=" + run.getStatus());
                    if (run.getStatus().equals(ThreadRun.RunStatus.REQUIRES_ACTION)) {
                        var toolCalls = run.getRequiredAction().getSubmitToolOutputs().getToolCalls();
                        var toolOutputs = functionExecutor.executeAll(toolCalls,
                                (toolCallId, result) -> ThreadRunSubmitOutputRequest.ToolOutput.builder()
                                        .toolCallId(toolCallId)
                                        .output(result)
                                        .build());
                        var runSubmitToolStream = openAI.threadRuns()
                                .submitToolOutputStream(threadId, run.getId(), ThreadRunSubmitOutputRequest.builder()
                                        .toolOutputs(toolOutputs)
                                        .stream(true)
                                        .build())
                                .join();
                        _handleRunEvents(threadId, runSubmitToolStream);
                    }
                    break;
                case EventName.THREAD_MESSAGE_DELTA:
                    var msgDelta = (ThreadMessageDelta) event.getData();
                    var content = msgDelta.getDelta().getContent().get(0);
                    if (content instanceof ContentPart.ContentPartTextAnnotation) {
                        var textContent = (ContentPart.ContentPartTextAnnotation) content;
                        System.out.print(textContent.getText().getValue());
                        sb.append(textContent.getText().getValue());
                    }
                    break;
                case EventName.THREAD_MESSAGE_COMPLETED:
                    System.out.println();
                    break;
                default:
                    break;
            }
        });

        return sb.toString();
    }



    //======== Basic Manage Function ========//
    public void deleteConversaction(String threadId){
        var deletedThread = openAI.threads().delete(threadId).join();
        log.info("Thread: "+ deletedThread.getId() +" was deleted: " + deletedThread.getDeleted());
    }

    public List<Image> imageGeneration(String promptStr) {
        var imageRequest = ImageRequest.builder()
                .prompt(promptStr)
                .n(1)
                .size(Size.X1024)
                .responseFormat(ImageResponseFormat.URL)
                .model(model.dall_e_3)
                .build();
        var futureImage = openAI.images().create(imageRequest);
        var imageResponse = futureImage.join();
        imageResponse.stream().forEach(img -> log.info("\n" + img.getUrl()));
        return imageResponse;
    }
    //========  ========//
}

package com.shang.vl.image;

import com.shang.vl.handler.ResponseHandler;
import com.shang.vl.model.Coordinate;
import com.shang.vl.model.InipResponse;
import com.shang.vl.prompt.PromptKeys;
import com.shang.vl.prompt.PromptManager;
import com.shang.vl.service.ImageUploadService;
import com.shang.vl.util.ImageUtil;
import com.shang.vl.util.InipRequestUtil;
import com.shang.vl.workflow.RequirementWorkflow;
import com.shang.vl.workflow.WorkflowMode;
import com.shang.vl.workflow.WorkflowRequest;
import com.shang.vl.workflow.WorkflowResult;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.input.PromptTemplate;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Created by shangwei2009@hotmail.com on 2025/8/29 16:00
 */
@SpringBootTest
public class VLModelTests {

    @Resource(name = "inipWebClient")
    private WebClient inipWebClient;

    @Resource(name = "streamingVLModel")
    private StreamingChatModel streamingVLModel;

    @Resource
    private ImageUploadService imageUploadService;

    @Resource
    private RequirementWorkflow requirementWorkflow;

    @Resource
    private PromptManager promptManager;


    @Test
    public void imageToTestPoint() throws IOException {
        final Path path = Paths.get(System.getProperty("user.dir"), "storage/case_detail3.png");
        final InipResponse inipResponse = InipRequestUtil.uploadImage(inipWebClient, Files.readAllBytes(path));
        final String objectKey = inipResponse.getMsgBody().getObjectKey();
        final String imageUrl = "http://images.chinaums.com?fileId=%s".formatted(objectKey);
        final String text = PromptTemplate.from("""
                        这是一张原型图，请根据图中的内容，提取所有控件和功能的测试点，以用于后续测试用例生成。
                        注意：
                        1.提取的测试点只基于图中的元素，不要发散。
                        2.不考虑浏览器、网络等错误场景。
                        """).template();
        final UserMessage userMessage = UserMessage.from(
                ImageContent.from(imageUrl, ImageContent.DetailLevel.HIGH),
                TextContent.from(text)
        );

        final ResponseHandler responseHandler = new ResponseHandler();
        streamingVLModel.chat(ChatRequest.builder()
                .messages(userMessage)
                .build(), responseHandler);

        System.out.println(responseHandler.toString());
        System.out.println(responseHandler.getResponseText());
    }

    @Test
    public void flowchartToTestPoint() throws IOException {
        final Path path = Paths.get(System.getProperty("user.dir"), "storage/flowchart1.png");
        final InipResponse inipResponse = InipRequestUtil.uploadImage(inipWebClient, Files.readAllBytes(path));
        final String objectKey = inipResponse.getMsgBody().getObjectKey();
        final String imageUrl = "http://images.chinaums.com?fileId=%s".formatted(objectKey);
        final String text = PromptTemplate.from("""
                        这是一个流程图，请根据图中的内容，提取所有测试点，以用于后续测试用例生成。
                        注意：
                        1.提取的测试点只基于图中的元素，不要发散。
                        """).template();
        final UserMessage userMessage = UserMessage.from(
                ImageContent.from(imageUrl, ImageContent.DetailLevel.HIGH),
                TextContent.from(text)
        );

        final ResponseHandler responseHandler = new ResponseHandler();
        streamingVLModel.chat(ChatRequest.builder()
                .messages(userMessage)
                .build(), responseHandler);

//        System.out.println(responseHandler.toString());
        System.out.println(responseHandler.getResponseText());
    }

    @Test
    public void imageToDemand() throws IOException {
//        final Path path = Paths.get(System.getProperty("user.dir"), "storage/微信支付.png");
        final Path path = Paths.get(System.getProperty("user.dir"), "storage/flowchart1.png");
        final String imageUrl = imageUploadService.uploadAndGetImageUrl(path);

        final WorkflowResult workflowResult = requirementWorkflow.run(new WorkflowRequest()
                .setImageUrl(imageUrl)
                .setOriginalDemand("")
                .setMode(WorkflowMode.REQUIREMENT_ONLY));

        final String requirement = workflowResult.getRequirement();
        System.out.println(requirement);

        final Path outputDir = Paths.get(System.getProperty("user.dir"), "storage");
        Files.createDirectories(outputDir);
        final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        final String imageFileName = path.getFileName().toString();
        final int dotIndex = imageFileName.lastIndexOf('.');
        final String baseName = dotIndex > 0 ? imageFileName.substring(0, dotIndex) : imageFileName;
        final Path outputPath = outputDir.resolve(baseName + ".md");
        final String markdown = """
                ## imageToDemand 输出（%s）

                %s

                ---

                """.formatted(timestamp, requirement == null ? "" : requirement);
        Files.writeString(outputPath, markdown);
    }

    @Test
    public void previewImageToDemandPrompt() {
        final String renderedPrompt = promptManager.render(PromptKeys.IMAGE_TO_DEMAND, Map.of(
                "oriDemand", "这里放原始需求文本，验证模板渲染效果"
        ));
        System.out.println(renderedPrompt);
    }

    @Test
    public void imageAddToDemand() throws IOException {
        final Path path = Paths.get(System.getProperty("user.dir"), "storage/TFMS1.png");
        String oriDemand="TFMS收到撤销惩戒指令后，立即反馈反诈一体化平台“处置成功”，当TFMS反馈反诈一体化平台6次重发都未收到305报文时，允许操作台修改编辑指令手动重发，直到TFMS收到305报文为止。";
        final InipResponse inipResponse = InipRequestUtil.uploadImage(inipWebClient, Files.readAllBytes(path));
        final String objectKey = inipResponse.getMsgBody().getObjectKey();
        final String imageUrl = "http://images.chinaums.com?fileId=%s".formatted(objectKey);
        final String text = PromptTemplate.from("""
                        请识别图中的内容，提取图中的信息并合并到原始需求描述中，以用于后续测试用例生成。
                        注意：
                        1.不要改动原始需求
                        2.只提取图中的元素信息，并合并到原始需求，不要发散
                        3.不考虑性能需求、不考虑网络异常需求
                        4.只返回需求描述内容
                        
                        # 原始需求描述如下:{{oriDemand}}
                        """) .apply(Map.of("oriDemand", oriDemand)).text();
        final UserMessage userMessage = UserMessage.from(
                ImageContent.from(imageUrl, ImageContent.DetailLevel.HIGH),
                TextContent.from(text)
        );

        final ResponseHandler responseHandler = new ResponseHandler();
        streamingVLModel.chat(ChatRequest.builder()
                .messages(userMessage)
                .build(), responseHandler);

        responseHandler.getResponseText();
    }

    @Test
    public void imageToTestCasesAndReview() throws IOException {
//        final Path path = Paths.get(System.getProperty("user.dir"), "storage/微信支付.png");
//        final Path path = Paths.get(System.getProperty("user.dir"), "storage/873d23de41c947d2b3f221a894a8b1ac.png");
//        final Path path = Paths.get(System.getProperty("user.dir"), "storage/case_detail.png");
        final Path path = Paths.get(System.getProperty("user.dir"), "storage/origin.jpg");
//        final String oriDemand = "TFMS收到撤销惩戒指令后，立即反馈反诈一体化平台“处置成功”，当TFMS反馈反诈一体
//        化平台6次重发都未收到305报文时，允许操作台修改编辑指令手动重发，直到TFMS收到305报文为止。";
        final String oriDemand = "";
        final String imageUrl = imageUploadService.uploadAndGetImageUrl(path);

        final WorkflowResult workflowResult = requirementWorkflow.run(new WorkflowRequest()
                .setImageUrl(imageUrl)
                .setOriginalDemand(oriDemand)
                .setMode(WorkflowMode.FULL_PIPELINE));

        final String requirement = workflowResult.getRequirement();
        System.out.println(requirement);
        final Path outputDir = Paths.get(System.getProperty("user.dir"), "storage");
        Files.createDirectories(outputDir);
        final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        final String imageFileName = path.getFileName().toString();
        final int dotIndex = imageFileName.lastIndexOf('.');
        final String baseName = dotIndex > 0 ? imageFileName.substring(0, dotIndex) : imageFileName;
        final Path outputPath = outputDir.resolve(baseName + ".md");
        final String markdown = """
                ## imageToDemand 输出（%s）

                %s

                ---

                """.formatted(timestamp, requirement == null ? "" : requirement);
        Files.writeString(outputPath, markdown);

//        System.out.println("===需求文档===");
//        System.out.println(workflowResult.getRequirement());
//        System.out.println("===测试用例===");
//        System.out.println(workflowResult.getTestCases());
//        System.out.println("===评审结果===");
//        System.out.println(workflowResult.getReview());
    }

    @Test
    public void demandToTestCasesAndReview() throws IOException {
        final String oriDemand = "";

        final WorkflowResult workflowResult = requirementWorkflow.run(new WorkflowRequest()
                .setImageUrl("")
                .setOriginalDemand(oriDemand)
                .setMode(WorkflowMode.REQUIREMENT_AND_TEST_CASES));

        final String requirement = workflowResult.getRequirement();

//        System.out.println("===需求文档===");
//        System.out.println(workflowResult.getRequirement());
//        System.out.println("===测试用例===");
//        System.out.println(workflowResult.getTestCases());
//        System.out.println("===评审结果===");
//        System.out.println(workflowResult.getReview());
    }
}

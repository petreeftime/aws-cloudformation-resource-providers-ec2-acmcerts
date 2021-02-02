package software.amazon.ec2.enclavecertificateiamroleassociation;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.GetAssociatedEnclaveCertificateIamRolesRequest;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<Ec2Client> proxyClient;

    @Mock
    private Ec2Client ec2Client;

    private ReadHandler handler;

    @BeforeEach
    public void setup() {
        handler = spy(new ReadHandler());
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        ec2Client = mock(Ec2Client.class);
        proxyClient = MOCK_PROXY(proxy, ec2Client);
    }

    @Test
    public void testGetEnclaveCertRoleArnAssociationSuccess() {
        final ResourceModel model = ResourceModel.builder()
                .certificateArn(TestUtils.CERTIFICATE_ARN)
                .roleArn(TestUtils.ROLE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client()
                .getAssociatedEnclaveCertificateIamRoles(any(GetAssociatedEnclaveCertificateIamRolesRequest.class)))
                .thenReturn(TestUtils.createGetAssociationsResponse());

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel()).isNotNull();

        ResourceModel returnModel = response.getResourceModel();
        assertThat(returnModel.getCertificateS3BucketName()).isEqualTo(TestUtils.CERTIFICATE_S3_BUCKET_NAME);
        assertThat(returnModel.getCertificateS3ObjectKey()).isEqualTo(TestUtils.CERTIFICATE_S3_OBJECT_KEY);
        assertThat(returnModel.getEncryptionKmsKeyId()).isEqualTo(TestUtils.ENCRYPTION_KMS_KEY_ID);
    }

    @Test
    public void testGetEnclaveCertRoleArnAssociationWithoutCert() {
        final ResourceModel model = ResourceModel.builder()
                .roleArn(TestUtils.ROLE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request =
                TestUtils.createAssociationRequest(model);
        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, proxyClient, logger);
        final String ERROR_MESSAGE = String.format(BaseHandlerStd.PROPERTY_CANNOT_BE_EMPTY,
                "CertificateArn");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).contains(ERROR_MESSAGE);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
    }

    @Test
    public void testGetEnclaveCertIamRoleAssociationWithoutIamRole() {
        final ResourceModel model = ResourceModel.builder()
                .certificateArn(TestUtils.CERTIFICATE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request =
                TestUtils.createAssociationRequest(model);
        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, proxyClient, logger);
        final String ERROR_MESSAGE = String.format(BaseHandlerStd.PROPERTY_CANNOT_BE_EMPTY,
                "RoleArn");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).contains(ERROR_MESSAGE);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
    }
}
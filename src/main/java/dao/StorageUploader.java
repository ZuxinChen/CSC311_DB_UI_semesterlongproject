package dao;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

public class StorageUploader {

    private BlobContainerClient containerClient;
    String storageKey = "DefaultEndpointsProtocol=https;AccountName=csc311storagechen;AccountKey=AqjyjM9z0mmZKyqpJy7DIZT3/KQA8jN26stjyiL9DSNuptE3l98/c//Fn6Y4LsduXVeo9ZCOnOgC+ASt8ljoYg==;EndpointSuffix=core.windows.net";
    public StorageUploader( ) {
        this.containerClient = new BlobContainerClientBuilder()
                .connectionString(storageKey)
                .containerName("media-files")
                .buildClient();
    }

    public void uploadFile(String filePath, String blobName) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.uploadFromFile(filePath);
    }
    public BlobContainerClient getContainerClient(){
        return containerClient;
    }
}
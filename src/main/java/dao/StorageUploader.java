package dao;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.BlobInfo;

public class StorageUploader {

    private BlobContainerClient containerClient;
    String storageKey = "DefaultEndpointsProtocol=https;AccountName=csc311storagechen;AccountKey=AqjyjM9z0mmZKyqpJy7DIZT3/KQA8jN26stjyiL9DSNuptE3l98/c//Fn6Y4LsduXVeo9ZCOnOgC+ASt8ljoYg==;EndpointSuffix=core.windows.net";
    public StorageUploader( ) {
        this.containerClient = new BlobContainerClientBuilder()
                .connectionString(storageKey)
                .containerName("media-files")
                .buildClient();
    }

    public BlobClient uploadFile(String filePath, String blobName) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.uploadFromFile(filePath);
        return blobClient;
    }

    public ObservableList<BlobInfo> listBlobInfos() {
        ObservableList<BlobInfo> infos = FXCollections.observableArrayList();
        for (BlobItem blobItem : containerClient.listBlobs()) {
            String url = containerClient.getBlobClient(blobItem.getName()).getBlobUrl();
            infos.add(new BlobInfo(blobItem.getName(),url));
        }
        return infos;
    }

    public BlobContainerClient getContainerClient(){
        return containerClient;
    }

}
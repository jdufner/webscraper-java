package de.jdufner.webscraper.crawler.image;

import de.jdufner.webscraper.crawler.data.DownloadedImage;
import de.jdufner.webscraper.crawler.data.Image;
import de.jdufner.webscraper.crawler.data.ImageState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ImageDownloaderIT {

    @Autowired
    private ImageDownloader imageDownloader;

    @Test
    void given_configuration_when_download_expect_output_file_created() {
        // arrange
        URI uri = URI.create("https://apod.nasa.gov/apod/image/2409/iss071e564695_4096.jpg");
        Image image = new Image(1, uri, ImageState.INITIALIZED);
        String fileName = null;

        try {
            // act
            DownloadedImage downloadedImage = imageDownloader.download(image);
            fileName = downloadedImage.fileName();

            // assert
            assertThat(downloadedImage.fileName()).isNotNull();
        } finally {
            if (fileName != null) {
                File file = new File(fileName);
                file.delete();
            }
        }
    }

    @Test
    void given_configuration_when_download_two_images_expect_second_file_faster() {
        // arrange
//        String[] urls = new String[] {
//                "https://apod.nasa.gov/apod/image/1103/lroc_wac_nearside.jpg",
//                "https://apod.nasa.gov/apod/image/2505/IssTransit_Sanz_2569.jpg",
//                "https://apod.nasa.gov/apod/image/2506/25BrightestStars_Jittasaiyapan_1500.jpg",
//                "https://apod.nasa.gov/apod/image/2506/APODStarryNight30thanniversary.jpg",
//                "https://apod.nasa.gov/apod/image/2506/Arp273Main_HubblePestana_3079.jpg",
//                "https://apod.nasa.gov/apod/image/2506/IssMoon_Holland_1063.jpg",
//                "https://apod.nasa.gov/apod/image/2506/RosettaDeepRed_Mendez_3294.jpg",
//                "https://apod.nasa.gov/apod/image/2506/TSE2023-Comp48-2a.jpg",
//                "https://apod.nasa.gov/apod/image/2506/farside_lro1600.jpg",
//                "https://apod.nasa.gov/apod/image/2507/CatsPaw_Webb_1822.jpg",
//                "https://apod.nasa.gov/apod/image/2507/Helix_GC_2332.jpg",
//                "https://apod.nasa.gov/apod/image/2507/LUA_JULHO_25_2048.jpg",
//                "https://apod.nasa.gov/apod/image/2507/LightningVolcano_Montufar_3000.jpg",
//                "https://apod.nasa.gov/apod/image/2507/NLCreflectionsHeden.jpg",
//                "https://apod.nasa.gov/apod/image/2507/Rosette_Decam_4000.jpg",
//                "https://apod.nasa.gov/apod/image/2508/Crab_HubbleChandraSpitzer_3600.jpg",
//                "https://apod.nasa.gov/apod/image/2508/NGC6872_block.jpg",
//                "https://apod.nasa.gov/apod/image/2508/PerseidsRadiant_Marcin_5500.jpg",
//                "https://apod.nasa.gov/apod/image/2508/Spiral1309_HubbleGalbany_4000.jpg",
//                "https://apod.nasa.gov/apod/image/2509/CometLemmon_DeWinter_3549.jpg",
//                "https://apod.nasa.gov/apod/image/2509/IMAP-IG2-001.JPG",
//                "https://apod.nasa.gov/apod/image/2509/JetIss_nasa_6604.jpg",
//                "https://apod.nasa.gov/apod/image/2509/OrionHorseHead_Stern_5842.jpg",
//                "https://apod.nasa.gov/apod/image/2509/SagNebs_DeWinter_4550.jpg",
//                "https://apod.nasa.gov/apod/image/2509/UmbraEarth.jpg",
//                "https://apod.nasa.gov/apod/image/2510/WitchBroom_Meyers_6043.jpg"
//        };
        String[] urls = new String[] {
                "https://apod.nasa.gov/apod/image/1103/lroc_wac_nearside.jpg",
                "https://apod.nasa.gov/apod/image/2505/IssTransit_Sanz_2569.jpg",
                "https://apod.nasa.gov/apod/image/2506/25BrightestStars_Jittasaiyapan_1500.jpg",
                "https://apod.nasa.gov/apod/image/2506/APODStarryNight30thanniversary.jpg",
                "https://apod.nasa.gov/apod/image/2506/Arp273Main_HubblePestana_3079.jpg"
        };
        List<String> fileNames = new ArrayList<>();

        try {
            // act
            Arrays.stream(urls).forEach(url -> {
                URI uri = URI.create(url);
                Image image = new Image(1, uri, ImageState.INITIALIZED);
                DownloadedImage downloadedImage = imageDownloader.download(image);
                fileNames.add(downloadedImage.fileName());
            });
        } finally {
            fileNames.forEach(fileName -> {
                File file = new File(fileName);
                file.delete();
            });
        }
    }

}

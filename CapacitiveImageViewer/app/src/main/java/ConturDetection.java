import org.hcilab.libftsp.capacitivematrix.blobdetection.BlobBoundingBox;
import org.opencv.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.CV_RETR_TREE;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.threshold;

public class ConturDetection {

    public static int[][] getBlobContentIn27x15(int[][] matrix, BlobBoundingBox bbb) {
        Mat image = new Mat();
        Mat inv_image = new Mat();
        Core.bitwise_not(image, inv_image);
        double thresh = threshold(inv_image, image, 200, 255, THRESH_BINARY);
        List<MatOfPoint> contours = new ArrayList<>();
        // anders als findContours von python??
        findContours(image, contours, hierarchy, mode=CV_RETR_TREE=3,method=CV_CHAIN_APPROX_SIMPLE=2);

        List<MatOfPoint> contours_filtered = new ArrayList<>();
        Mat max_contour = new Mat();
        for (int i = 0; i < contours.size(); i++) {
            if (contourArea(contours.get(i)) > 5 && contourArea(contours.get(i)) > 255) {
                contours_filtered.add(contours.get(i));
                if (contourArea(contours.get(i)) > contourArea(max_contour)) {
                    max_contour = contours.get(i);
                }

            }
        }

        if (!contours_filtered.isEmpty()) {
            int[][] croped_im = new int[27][15];
            int xmin = 0;
            int xmax = 0;
            int ymin = 0;
            int ymax = 0;
            // hier kommt ne haessliche for schleife hin
            return null;
        }
        return null;
        /*
        crop_frame = 5

        def detect_blobs(image):
            image = image.reshape(27, 15)
            temp, thresh = cv2.threshold(cv2.bitwise_not(image), 200, 255, cv2.THRESH_BINARY)
            im2, contours, hierarchy = cv2.findContours(thresh, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
            contours = [a for a in contours if cv2.contourArea(a) > 5 and cv2.contourArea(a) < 255]
            if len(contours) > 0:
                max_contour = max(contours, key=lambda a: cv2.contourArea(a))
                #M = cv2.moments(max_contour)
                #cx = round(M['m10'] / M['m00'])
                #cy = round(M['m01'] / M['m00'])
                #croped_im = image[max(cy - crop_frame, 0):min(cy + crop_frame, 27), max(cx - crop_frame, 0):min(cx + crop_frame, 15)].copy()
                # pad border
                #croped_im = cv2.copyMakeBorder(croped_im, max(-1 * (cy - crop_frame), 0), max(-1 * (image.shape[0] - (cy + crop_frame)), 0), max(-1 * (cx - crop_frame), 0), max(-1 * (image.shape[1] - (cx + crop_frame)), 0), cv2.BORDER_CONSTANT, value=0)
                croped_im = np.zeros((27,15))
                croped_im[0:ymax-ymin+2,0:xmax-xmin+2] = image[ymin-1:ymax+1,xmin-1:xmax+1]
                return (1, [croped_im])
            else:
                return (0, np.zeros((27,15)))
         */
    }
}

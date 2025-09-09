//package util;
//
//import org.opencv.core.Mat;
//import org.opencv.core.MatOfRect;
//import org.opencv.core.Point;
//import org.opencv.core.Rect;
//import org.opencv.videoio.VideoCapture;
//import org.opencv.imgproc.Imgproc;
//import org.opencv.objdetect.CascadeClassifier;
//import org.opencv.imgcodecs.Imgcodecs;
//
//public class CapturaRosto {
//    static {
//        System.load("C:/CPMA/src/main/java/util/opencv/build/java/x64/opencv_java4110.dll");
//    }
//
//    public static void main(String[] args) {
//        System.out.println("Tentando acessar a câmera...");
//        VideoCapture camera = new VideoCapture(0);
//        System.out.println("Câmera aberta: " + camera.isOpened());
//
//        CascadeClassifier detectorRosto = new CascadeClassifier("C:/CPMA/src/main/java/util/opencv/sources/data/haarcascades/haarcascade_frontalface_alt.xml");
//        if (detectorRosto.empty()) {
//            System.out.println("Erro ao carregar o classificador de rostos.");
//            return;
//        }
//
//        if (!camera.isOpened()) {
//            System.out.println("Erro ao abrir a câmera.");
//            return;
//        }
//
//        Mat frame = new Mat();
//        int contador = 0;
//
//        while (contador < 10) {
//            if (camera.read(frame)) {
//                System.out.println("Frame capturado.");
//
//                Mat imagemCinza = new Mat();
//                Imgproc.cvtColor(frame, imagemCinza, Imgproc.COLOR_BGR2GRAY);
//
//                MatOfRect rostos = new MatOfRect();
//                detectorRosto.detectMultiScale(imagemCinza, rostos);
//                System.out.println("Rostos detectados: " + rostos.toArray().length);
//
//                for (Rect ret : rostos.toArray()) {
//                    Imgproc.rectangle(frame, new Point(ret.x, ret.y),
//                            new Point(ret.x + ret.width, ret.y + ret.height),
//                            new org.opencv.core.Scalar(0, 255, 0));
//
//                    Mat rosto = new Mat(frame, ret);
//                    Imgcodecs.imwrite("C:/CPMA/src/resources/imagens/rosto" + contador + ".png", rosto);
//                    System.out.println("Imagem salva: usuario_" + contador + ".png");
//                    contador++;
//                }
//            }
//        }
//
//        camera.release();
//        System.out.println("Captura finalizada.");
//    }
//}

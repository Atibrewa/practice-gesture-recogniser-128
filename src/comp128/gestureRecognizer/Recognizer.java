package comp128.gestureRecognizer;

import Graphics.Point;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Recognizer to recognize 2D gestures. Uses the $1 gesture recognition algorithm.
 */
public class Recognizer {
    private final int RESAMPLE_SIZE = 64;
    private final double SCALE_SIZE = 200;
    private final Point CENTRE_POINT = new Point(0, 0);

    private List<Template> templates = new ArrayList<>();

    /**
     * Constructs a recognizer object
     */
    public Recognizer(){
    }

    /**
     * Create a template to use for matching
     * @param name of the template
     * @param points in the template gesture's path
     */
    public void addTemplate(String name, Deque<Point> points){
        Deque<Point> processedPoints = prepPoints(points);
        Template t = new Template(processedPoints, name);
        templates.add(t);
    }

    /**
     * Resamples the deque to give a new one with a specific number 
     * of points at regular intervals along the path of the gesture.
     * Creates new points where needed to ensure equal spacing
     * @param originalPoints The deque that needs to be resampled
     * @param n the number of points that it will be resampled to
     * @return the resampled deque with n points
     */
    protected Deque<Point> resample(Deque<Point> originalPoints, int n) {
        Deque<Point> resampledDeque = new ArrayDeque<Point>();
        double pathLength = pathLength(originalPoints);
        double resampleInterval = pathLength/(n-1);
        resampledDeque.add(originalPoints.peek());
        double accumulatedDistance = 0;
        Iterator<Point> p = originalPoints.iterator();
        Point p1 = p.next();
        Point p2 = p.next();
        while (p.hasNext()) {
            double segmentDistance = p2.distance(p1);
            if ((accumulatedDistance + segmentDistance) < resampleInterval) {
                accumulatedDistance += p2.distance(p1);
                p1 = p2;
                p2 = p.next();
            } else {
                Point newPoint = Point.interpolate(p1, p2, (resampleInterval - accumulatedDistance)/segmentDistance);
                resampledDeque.add(newPoint);
                p1 = newPoint;
                accumulatedDistance = 0;
            } 
        }
        if (resampledDeque.size() < n) {
            resampledDeque.add(originalPoints.getLast());
        }
        return resampledDeque;
    }

   /**
    * Calculates the total distance between all points in the deque,
    * effectively giving the path length
    * @param points the deque of points for which path needs to be calculated
    * @return length of gesture's path
    */
    protected double pathLength(Deque<Point> points) {
        double pathLength = 0;
        Point p1 = points.peek();
        for (Point p2 : points) {
            pathLength += p2.distance(p1);
            p1 = p2;
        }
        return pathLength;
    }

    /**
     * Calculates the centroid of all the points in a given deque
     * @param resampledDeque the deque of points for which centroid needs to be calculated
     * @return the centroid of the gesture
     */
    protected Point centroid(Deque<Point> resampledDeque) {
        double xTotal = 0;
        double yTotal = 0;
        for (Point p : resampledDeque) {
            xTotal += p.getX();
            yTotal += p.getY();
        }
        double x = xTotal/resampledDeque.size();
        double y = yTotal/resampledDeque.size();
        Point centroid = new Point(x, y);
		return centroid;
	}

    /**
     * Calculated the angle that the centroid and the starting point of the gesture make
     * with the horizontal
     * @param resampledDeque the deque for which indicative angle needs to be calculated
     * @return the indicative angle
     */
    protected double indicativeAngle(Deque<Point> resampledDeque) {
        Point centroid = centroid(resampledDeque);
        Point anglePoint = centroid.subtract(resampledDeque.peek());
        Double angle = anglePoint.angle();
        return angle;
    }

    /**
     * Rotates every point in the deque by the given angle, around the 
     * centroid of the gesture
     * @param resampledDeque the deque of points that needs to be rotated
     * @param pi the angle that the gesture needs to be rotated by
     * @return a new deque of points that has been rotated
     */
    protected Deque<Point> rotateBy(Deque<Point> resampledDeque, double pi) {
        Point centroid = centroid(resampledDeque);
        Deque<Point> rotatedDeque = new ArrayDeque<>(); 
        for (Point p : resampledDeque) {
            rotatedDeque.add(p.rotate(pi, centroid));
        }
        return rotatedDeque;
    }

    /**
     * Calculates the height and width of the gesture and saves it as the X and Y of a new 
     * Point which is returned
     * @param rotatedDeque the deque for which bounding box needs to be calculated
     * @return a point that indicates the bounding box
     */
    protected Point calculateBoundingBox(Deque<Point> rotatedDeque) {
        Point maxPoint = rotatedDeque.peek();
        Point minPoint = rotatedDeque.peek();
        for (Point p : rotatedDeque) {
            maxPoint = Point.max(p, maxPoint);
            minPoint = Point.min(p, minPoint);
        }
        Point boundingBox = maxPoint.subtract(minPoint);
        return boundingBox;
    }

    /**
     * Scales each point in the deque to the given size to make the gesture comparable
     * @param rotatedDeque the deque that needs to be scaled
     * @param size the size that deque needs to be scaled to
     * @return a deque of scaled points
     */
    protected Deque<Point> scaleTo(Deque<Point> rotatedDeque, double size) {
        Deque<Point> scaledDeque = new ArrayDeque<>();
        Point boundingBox = calculateBoundingBox(rotatedDeque);
        double width = boundingBox.getX();
        double height = boundingBox.getY();
        for (Point p : rotatedDeque) {
            scaledDeque.add(p.scale(size/width, size/height));
        }
        return scaledDeque;
    }

    /**
     * Translates every point in the deque to a new one, relative to the anchor point
     * @param scaledDeque the deque that needs to be translated
     * @param k the anchor point that the gesture is translated to
     * @return a deque with translated points
     */
    protected Deque<Point> translateTo(Deque<Point> scaledDeque, Point k) {
        Deque<Point> translatedDeque = new ArrayDeque<>();
        Point centroid = centroid(scaledDeque);
        for (Point p : scaledDeque) {
            translatedDeque.add(p.add(k).subtract(centroid));
        }
        return translatedDeque;
    }

    private Deque<Point> prepPoints(Deque<Point> originalPoints) {
        Deque<Point> resampledDeque = resample(originalPoints, RESAMPLE_SIZE);
        double pi = indicativeAngle(resampledDeque);
        Deque<Point> rotatedDeque = rotateBy(resampledDeque, -pi);
        Deque<Point> scaledDeque = scaleTo(rotatedDeque, SCALE_SIZE);
        Deque<Point> processedPoints = translateTo(scaledDeque, CENTRE_POINT);
        return processedPoints;
    }

    /**
     * Passes the original deque through the required steps and compares it to all 
     * available templates to find the best match
     * @param originalPoints the deque of points that needs to be recognised
     * @return a BestMatch containing the template and score that the gesture is closest to
     */
    public BestMatch recognize(Deque<Point> originalPoints) {
        Deque<Point> processedGesture = prepPoints(originalPoints);
        double minDistance = Double.MAX_VALUE;
        if (templates.size() != 0) {
            Template closestMatch = templates.get(0);
            for (Template template : templates) {
                Double distance = distanceAtBestAngle(processedGesture, template.getPoints());
                if (distance < minDistance) {
                    minDistance = distance;
                    closestMatch = template;
                }
            }
            double score = 1 - (2*minDistance/Math.sqrt(2* Math.pow(SCALE_SIZE,2)));
            BestMatch bestMatch = new BestMatch(closestMatch, score);
            return bestMatch;
        } else {
            return null;
        }        
    }

    /**
     * Uses a golden section search to calculate rotation that minimizes the distance between the gesture and the template points.
     * @param points
     * @param templatePoints
     * @return best distance
     */
    private double distanceAtBestAngle(Deque<Point> points, Deque<Point> templatePoints){
        double thetaA = -Math.toRadians(45);
        double thetaB = Math.toRadians(45);
        final double deltaTheta = Math.toRadians(2);
        double phi = 0.5*(-1.0 + Math.sqrt(5.0));// golden ratio
        double x1 = phi*thetaA + (1-phi)*thetaB;
        double f1 = distanceAtAngle(points, templatePoints, x1);
        double x2 = (1 - phi)*thetaA + phi*thetaB;
        double f2 = distanceAtAngle(points, templatePoints, x2);
        while(Math.abs(thetaB-thetaA) > deltaTheta){
            if (f1 < f2){
                thetaB = x2;
                x2 = x1;
                f2 = f1;
                x1 = phi*thetaA + (1-phi)*thetaB;
                f1 = distanceAtAngle(points, templatePoints, x1);
            }
            else{
                thetaA = x1;
                x1 = x2;
                f1 = f2;
                x2 = (1-phi)*thetaA + phi*thetaB;
                f2 = distanceAtAngle(points, templatePoints, x2);
            }
        }
        return Math.min(f1, f2);
    }

    private double distanceAtAngle(Deque<Point> points, Deque<Point> templatePoints, double theta){
        Deque<Point> rotatedPoints = null;
        rotatedPoints = rotateBy(points, theta);
        return pathDistance(rotatedPoints, templatePoints);
    }

    /**
     * Calculates the average distance between each of the corresponding points of two deques
     * @param a the first deque that needs to be compared
     * @param b the second deque that is being compared to
     * @return the averave distance between the gestures
     */
    protected double pathDistance(Deque<Point> a, Deque<Point> b){
        double distance = 0;
        Iterator<Point> p1 = a.iterator();
        Iterator<Point> p2 = b.iterator();
        while (p1.hasNext()) {
            distance += p1.next().distance(p2.next());
        }
        return distance/a.size();
    }

}
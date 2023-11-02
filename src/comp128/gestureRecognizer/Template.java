package comp128.gestureRecognizer;

import java.util.Deque;

import Graphics.Point;

/**
 * A class that contains the fully processed deque of points for a gesture as well as its name
 * so that it can be quickly used for the recogniser algorithm
 */
public class Template {
    private Deque<Point> points;
    private String name;

    /**
     * creates a new template that contains a gesture's points and its name
     * @param gesture gesture that is being saved as a template
     * @param name name of the gesture
     */
    public Template(Deque<Point> gesture, String name) {
        points = gesture;
        this.name = name;
    }

    /**
     * @return a deque of points (the points of the template gesture)
     */
    public Deque<Point> getPoints() {
        return points;
    }
    
    /**
     * @return name of the template
     */
    public String getName() {
        return name;
    }
}

package comp128.gestureRecognizer;

/**
 * A class that contains a template and the score that a gesture recieved against this template
 */
public class BestMatch {
    private Template template;
    private double score;

    /**
     * creates a new match that contains a template, and the score the gesture recieved against it
     * @param template template that a gesture was compared to
     * @param score the score the gesture got against this template
     */
    public BestMatch(Template template, double score) {
        this.template = template;
        this.score = score;
    }

    /**
     * @return the template that was matched
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * @return score of the gesture against the template
     */
    public double getScore() {
        return score;
    }
}

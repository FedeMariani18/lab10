package it.unibo.mvc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final int MIN = 0;
    private static final int MAX = 100;
    private static final int ATTEMPTS = 5;
    private static final String FILE_NAME = "config.yml";
    private final DrawNumber model;
    private final List<DrawNumberView> views;
    DrawNumberViewImpl view = new DrawNumberViewImpl();

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        int min = MIN;
        int max = MAX;
        int attempts = ATTEMPTS;

        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }

        try (InputStreamReader inputSR= new InputStreamReader(ClassLoader.getSystemResourceAsStream(FILE_NAME))) {
            int c;
            int i = 0;
            final StringBuilder sb = new StringBuilder();
            
            while ((c = inputSR.read()) != -1) {//NOPMD best way to read char by char 
                sb.append((char) c);
            }
            
            final StringTokenizer st = new StringTokenizer(sb.toString());
            while (st.hasMoreTokens()) { 
                switch (i) { 
                    case 1: 
                        min = Integer.parseInt(st.nextToken());
                        break;
                    case 3: 
                        max = Integer.parseInt(st.nextToken());
                        break;
                    case 5:
                        attempts = Integer.parseInt(st.nextToken()); 
                        break;
                    default: 
                        st.nextToken(); 
                }
                i++;
            }
        } catch (IOException e) {
            view.displayError(e.getMessage());
        }
        //set of the configuration
        this.model = new DrawNumberImpl(min, max, attempts);
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl(), new PrintStreamView(System.out));
    }

}

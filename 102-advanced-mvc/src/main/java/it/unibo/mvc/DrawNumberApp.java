package it.unibo.mvc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final int MIN = 0;
    private static final int MAX = 100;
    private static final int ATTEMPTS = 10;
    private static final String SEP = File.separator;
    private static final String FILE_PATH = ".." + SEP + ".." + SEP + ".." + SEP + ".." 
        + SEP + "resources"+ SEP +"config.yml";

    private final DrawNumber model;
    private final List<DrawNumberView> views;
    DrawNumberViewImpl view = new DrawNumberViewImpl();

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }

        try (
            final InputStream file = new FileInputStream(new File(FILE_PATH));
            final InputStreamReader inputSR = new InputStreamReader(file);
        ) {
            int c;
            String text = null;
            while ((c = inputSR.read()) != -1) {
                text = text + c;
            }

            StringTokenizer st = new StringTokenizer(text);
            int i = 0;
            while (st.hasMoreTokens()) {
                switch (i) {
                    case 1: int min = Integer.parseInt(st.nextToken());
                    case 3: int max = Integer.parseInt(st.nextToken());
                    case 5: int attempts = Integer.parseInt(st.nextToken());
                    default: st.nextToken(); 
                }
            }
        } catch (Exception e) {
            view.displayError(e.getMessage());
        }

        //set of the configuration
        this.model = new DrawNumberImpl(MIN, MAX, ATTEMPTS);
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

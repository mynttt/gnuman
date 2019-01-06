package de.hshannover.inform.gnuman.app.rules;

/**
 * Rules for bonus items.
 * @author Marc Herschel
 */
public class BonusRules {

    /**
     * Points according to each level.
     * @param level level to evaluate for
     * @return according amount of bonus points.
     */
    public static int evaluate(int level) {
        if(level == 1) { return 100; }
        if(level == 2) { return 300; }
        if(level == 3) { return 500; }
        if(level == 4) { return 700; }
        if(level == 5) { return 1000; }
        if(level == 6) { return 2000; }
        if(level == 7) { return 3000; }
        return 5000;
    }

}

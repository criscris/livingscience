package utils.text.distance;


/*
 * As of 2007-08-23, the best algorithm known (to the author=mwyoung) for
 * short strings is one due to Eugene Myers, except for the special case
 * where the distance limit is 0 or 1.  The Myers algorithm also has good
 * worst-case performance for long strings when the edit distance is not
 * reasonably bounded.
 *
 * When there is a good bound, a variant of the Ukkonen algorithm due to
 * Berghel and Roach (modified by Michael Young to use linear space)
 * is faster for long strings.
 *
 * Note that other algorithms that perform better in some cases for running
 * text searches do not outperform Myers for rigid distance computations.
 * Notably:
 *   Navarro/Baeza-Yates (Algorithmica 23,2) simulates an NFA with an
 *   epsilon-cycle on the initial state (appropriate for running texts)
 *   and reports success without computing exact distance.  When adjusted
 *   to a fixed starting point and computing distance, its state machine
 *   is larger and it underperforms.
 *
 *   BITAP (Baeza-Yates/Gonnet, Manber/Wu) also simulates an NFA, and
 *   Navarro claims that it wins for small patterns and small limits for
 *   running search.  Experiments with a Java implementation showed that
 *   it beat Myers on pure string edit distance only for limits where the
 *   special 0-1 limit applied, where special-case comparison beats all.
 *
 * A survey of algorithms for running text search by Navarro appeared
 * in ACM Computing Surveys 33#1: http://portal.acm.org/citation.cfm?id=375365
 * Another algorithm (Four Russians) that Navarro claims superior for very
 * long patterns and high limits was not evaluated for inclusion here.
 * Filtering algorithms also improve running search, but do not help
 * for pure edit distance.
 */
public class Levenshtein implements GeneralEditDistance {
  /**
   * Long+bounded implementation class: distance-only Berghel-Roach.
   */
  private ModifiedBerghelRoachEditDistance berghel;

  /**
   * Short/unbounded implementation class: Myers bit-parallel.
   */
  private MyersBitParallelEditDistance myers;

  /**
   * Saved pattern, for specialized comparisons.
   */    
  private final CharSequence pattern;

  /**
   * Length of saved pattern.
   */
  private final int patternLength;

  public Levenshtein(CharSequence pattern) {
    this.pattern = pattern;
    this.patternLength = pattern.length();
  }

  public GeneralEditDistance duplicate() {
    Levenshtein dup = new Levenshtein(pattern);

    /* Duplicate the Myers engine, as it is cheaper than rebuilding */
    if (this.myers != null) {
      dup.myers = (MyersBitParallelEditDistance) this.myers.duplicate();
    }
    
    /* Do not duplicate the Berghel engine; it provides no savings. */

    return dup;
  }

  public int getDistance(CharSequence target, int limit) {
    /* When the limit is 0 or 1, specialized comparisons are much faster. */
    if (limit <= 1) {
      return limit == 0 ?
               (pattern.equals(target) ? 0 : 1) :
            	   GeneralEditDistances.atMostOneError(pattern, target);
    }

    /*
     * The best algorithm for long strings depends on the resulting
     * edit distance (or the limit placed on it).  Without further
     * information on the likelihood of a low distance, we guess
     * based on the provided limit.  We currently lean toward using
     * the Myers algorithm unless we are pretty sure that the
     * Berghel-Roach algorithm will win (based on the limit).
     *
     * Note that when the string lengths are small (fewer characters
     * than bits in a long), Myers wins regardless of limit.
     */
    if ((patternLength > 64)
        && (limit < (target.length() / 10))) {
      if (berghel == null) {
        berghel = ModifiedBerghelRoachEditDistance.getInstance(pattern);
      }
      return berghel.getDistance(target, limit);
    }

    if (myers == null) {
      myers = MyersBitParallelEditDistance.getInstance(pattern);
    }

    return myers.getDistance(target, limit);
  }
}

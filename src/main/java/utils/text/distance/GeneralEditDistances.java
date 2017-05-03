package utils.text.distance;

/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * A collection of instance generators for the GeneralEditDistance interface.
 */
public class GeneralEditDistances {
  /**
   * Chooses the best implementation of Levenshtein string edit distance
   * available at the current time.
   */


  /**
   * Compares two strings for at most one insert/delete/substitute difference.
   * Since operations cannot be composed, a simple case analysis is possible.
   *
   * @param s1 one string to be compared
   * @param s2 the other string to be compared
   * @return Levenshtein edit distance if no greater than 1;
   *         otherwise, more than 1
   */
  public static int atMostOneError(CharSequence s1, CharSequence s2) {
    int s1Length = s1.length();
    int s2Length = s2.length();
    int errors = 0;             /* running count of edits required */

    switch(s2Length - s1Length) {
      /*
       * Strings are the same length.  No single insert/delete is possible;
       * at most one substitution can be present.
       */
      case 0:
        for (int i = 0; i < s2Length; i++) {
          if ((s2.charAt(i) != s1.charAt(i)) && (errors++ != 0)) {
            break;
          }
        }
        return errors;

      /*
       * Strings differ in length by 1, so we have an insertion
       * (and therefore cannot have any other substitutions).
       */
      case 1: /* s2Length > s1Length */
        for (int i = 0; i < s1Length; i++) {
          if (s2.charAt(i) != s1.charAt(i)) {
            for (; i < s1Length; i++) {
              if (s2.charAt(i + 1) != s1.charAt(i)) {
                return 2;
              }
            }
            return 1;
          }
        }
        return 1;

      /* Same as above case, with strings reversed */
      case -1: /* s1Length > s2Length */
        for (int i = 0; i < s2Length; i++) {
          if (s2.charAt(i) != s1.charAt(i)) {
            for (; i < s2Length; i++) {
              if (s2.charAt(i) != s1.charAt(i + 1)) {
                return 2;
              }
            }
            return 1;
          }
        }
        return 1;

      /* Edit distance is at least difference in lengths; more than 1 here. */
      default:
        return 2;
    }
  }

  /**
   * Generates an GeneralEditDistance engine for a particular pattern string
   * based on Levenshtein distance.  Caller must ensure that the
   * pattern does not change (consider using pattern.toString() if
   * necessary) as long as the generated object is to be used.
   *
   * @param pattern a string from which distance computations are desired
   * @return an engine for computing Levenshtein distances from that pattern
   */
  public static GeneralEditDistance
      getLevenshteinDistance(CharSequence pattern) {
    return new Levenshtein(pattern);
  }

  private GeneralEditDistances() { }
}

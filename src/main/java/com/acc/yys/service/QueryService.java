package com.acc.yys.service;

import com.acc.yys.pojo.Character;
import com.acc.yys.pojo.CharacterDistribution;
import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zhaoyy on 2017/5/26.
 */
public interface QueryService {


    class QueryResult implements Comparable<QueryResult>, Serializable {

        private static final long serialVersionUID = 1L;

        private final String locationName;
        private final List<CharacterDistribution> distributions;
        private final int count;

        public QueryResult(String locationName, List<CharacterDistribution> distributions) {
            this.locationName = locationName;
            this.distributions = distributions;
            int count = 0;
            for (CharacterDistribution cd : distributions) {
                count += cd.getCount();
            }
            this.count = count;
        }

        public String getLocationName() {
            return locationName;
        }

        public List<CharacterDistribution> getDistributions() {
            return distributions;
        }

        public int getCount() {
            return count;
        }

        @Override
        public int compareTo(QueryResult o) {
            if (o.count == count)
                return o.locationName.compareTo(locationName);
            return Integer.compare(o.count, count);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("locationName", locationName)
                    .add("count", count)
                    .add("distributions", distributions)
                    .toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            QueryResult that = (QueryResult) o;

            if (count != that.count) return false;
            if (!locationName.equals(that.locationName)) return false;
            return distributions.equals(that.distributions);

        }

        @Override
        public int hashCode() {
            int result = locationName.hashCode();
            result = 31 * result + distributions.hashCode();
            result = 31 * result + count;
            return result;
        }
    }

    List<QueryResult> queryCharacterLocation(Character character);
}

package software.closure.s3webproxy

/*
 Copyright 2018 Closure Software S.L.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

/**
 * Default implementation of the User interface.
 *
 * Date: 24/3/18
 * Time: 0:09
 * @author narciso@closure.software
 * @since 0.1
 */
class DefaultUser implements User, Serializable, Comparable {

    String username
    String password

    boolean equals( final o ) {
        if( this.is( o ) ) return true
        if( !(o instanceof User) ) return false

        final User that = (User) o

        if( username != that.username ) return false

        return true
    }

    int hashCode() {
        return (username != null ? username.hashCode() : 0)
    }

    @Override
    String toString() {
        username
    }

    @Override
    int compareTo( final Object o ) {
        try {
            return username <=> ((User) o)?.username
        }
        catch( ignored ) {
            -1 // safe play?
        }
    }
}

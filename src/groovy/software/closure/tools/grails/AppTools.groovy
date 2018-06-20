package software.closure.tools.grails

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

import grails.util.Holders

/**
 * Useful app tools.
 *
 * Date: 13/11/17
 * Time: 11:51
 * @author narciso@closure.software
 * @since 0.1
 */
class AppTools {

    private static String RULE = "============================================================"

    static String getLogSectionMarker() {
        RULE
    }

    static String getAppCycleLogMessage( String event ) {
        getAppSectionLogMessage( "${getAppMetaData( 'app.name' )} v${getAppMetaData( 'app.version' )} $event" )
    }

    static String getAppSectionLogMessage( String message ) {
        """\n\n$RULE\n$message\n$RULE\n"""
    }

    static String getAppMetaData( String key ) {
        Holders.grailsApplication.metadata[key]
    }


}

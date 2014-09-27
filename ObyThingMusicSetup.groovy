/**
 *  ObyThing Music Setup
 *
 *  Copyright 2014 ObyCode
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "ObyThing Music Setup",
    namespace: "com.obycode",
    author: "cbycode",
    description: "Use this app to setup ObyThing Music (http://obything.obycode.com) to control your music with SmartThings.",
    category: "SmartThings Labs",
    iconUrl: "http://obycode.com/obything/ObyThingSTLogo.png",
    iconX2Url: "http://obycode.com/obything/ObyThingSTLogo@2x.png")


preferences {
    section("Setup my ObyThing with this IP:port (click the icon in the status bar to find this):") {
        input "theAddr", "string", multiple: false, required: true
    }
    section("on this hub...") {
        input "theHub", "hub", multiple: false, required: true
    }

}

def installed() {
    log.debug "Installed ${app.label} with address '${settings.theAddr}' on hub '${settings.theHub.name}'"

    initialize()
}

def updated() {
    /*
    log.debug "Updated ${app.label} with address '${settings.theAddr}' on hub '${settings.theHub.name}'"
    
    def current = getChildDevices()
    log.debug "children: $current"
    
    if (app.label != current.label) {
        log.debug "CHANGING name from ${current.label} to ${app.label}"
        log.debug "label props: ${current.label.getProperties()}"
        current.label[0] = app.label
    }
    */
}

def initialize() {
    def parts = theAddr.split(":")
    def iphex = convertIPtoHex(parts[0])
    def porthex = convertPortToHex(parts[1])
    def dni = "$iphex:$porthex"
    def hubNames = location.hubs*.name.findAll { it }
    def d = addChildDevice("com.obycode", "ObyThing Music", dni, theHub.id, [label:"${app.label}", name:"ObyThing"])
    log.trace "created ObyThing '${d.displayName}' with id $dni"
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
    return hex

}

private String convertPortToHex(port) {
    String hexport = port.toString().format( '%04X', port.toInteger() )
    return hexport
}

/**
 *  ObyThing Music
 *
 *  Copyright 2014 obycode
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

import groovy.json.JsonSlurper

metadata {
  definition (name: "ObyThing Music", namespace: "com.obycode", author: "obycode") {
    capability "Music Player"
    capability "Speech Synthesis"
    capability "Tone"
    capability "Refresh"
    capability "Switch"

    // These strings are comma separated lists of names
    attribute "playlists", "json_object"
    attribute "speakers", "json_object"

    // playPlaylist(String uri, speakers=null, volume=null, resume=false, restore=false)
    command "playPlaylist", ["string", "string", "number", "number", "number"]
    // playTrack(String uri, speakers=null, volume=null, resume=false, restore=false, playlist=null)
    command "playTrack", ["string", "string", "number", "number", "number", "string"]
  }

  simulator {
    // TODO: define status and reply messages here
  }

  tiles {
    // Main
    standardTile("main", "device.status", width: 1, height: 1, canChangeIcon: true) {
      state "paused", label:'Paused', action:"music Player.play", icon:"st.Electronics.electronics19", nextState:"playing", backgroundColor:"#ffffff"
      state "playing", label:'Playing', action:"music Player.pause", icon:"st.Electronics.electronics19", nextState:"paused", backgroundColor:"#79b821"
    }

    // Row 1
    standardTile("nextTrack", "device.status", width: 1, height: 1, decoration: "flat") {
      state "next", label:'', action:"music Player.nextTrack", icon:"st.sonos.next-btn", backgroundColor:"#ffffff"
    }
    standardTile("playpause", "device.status", width: 1, height: 1, decoration: "flat") {
      state "default", label:'', action:"music Player.play", icon:"st.sonos.play-btn", nextState:"playing", backgroundColor:"#ffffff"
      state "playing", label:'', action:"music Player.pause", icon:"st.sonos.pause-btn", nextState:"paused", backgroundColor:"#ffffff"
      state "paused", label:'', action:"music Player.play", icon:"st.sonos.play-btn", nextState:"playing", backgroundColor:"#ffffff"
    }
    standardTile("previousTrack", "device.status", width: 1, height: 1, decoration: "flat") {
      state "previous", label:'', action:"music Player.previousTrack", icon:"st.sonos.previous-btn", backgroundColor:"#ffffff"
    }

    // Row 2
    standardTile("airplay", "device.switch", width: 1, height: 1, decoration: "flat", canChangeIcon: true) {
      state "on", label:'AirPlay On', action:"switch.off", icon:"st.Electronics.electronics14", nextState:"off", backgroundColor:"#ffffff"
      state "off", label:'AirPlay Off', action:"switch.on", icon:"st.Electronics.electronics16", nextState:"on", backgroundColor:"#ffffff"
    }
    standardTile("status", "device.status", width: 1, height: 1, decoration: "flat", canChangeIcon: true) {
      state "playing", label:'Playing', action:"music Player.pause", icon:"st.Electronics.electronics19", nextState:"paused", backgroundColor:"#ffffff"
      state "stopped", label:'Stopped', action:"music Player.play", icon:"st.Electronics.electronics19", nextState:"playing", backgroundColor:"#ffffff"
      state "paused", label:'Paused', action:"music Player.play", icon:"st.Electronics.electronics19", nextState:"playing", backgroundColor:"#ffffff"
    }
    standardTile("mute", "device.mute", inactiveLabel: false, decoration: "flat") {
      state "unmuted", label:"Mute", action:"music Player.mute", icon:"st.custom.sonos.unmuted", backgroundColor:"#ffffff", nextState:"muted"
      state "muted", label:"Unmute", action:"music Player.unmute", icon:"st.custom.sonos.muted", backgroundColor:"#ffffff", nextState:"unmuted"
    }

    // Row 3
    controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false) {
      state "level", action:"music Player.setLevel", backgroundColor:"#ffffff"
    }

    // Row 4 - Disable this for now until we get communication back to hub working
    //      valueTile("currentSong", "device.trackDescription", inactiveLabel: true, height:1, width:3, decoration: "flat") {
    //          state "default", label:'${currentValue}', backgroundColor:"#ffffff"
    //      }

    // Row 5
    standardTile("refresh", "device.status", inactiveLabel: false, decoration: "flat") {
      state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh", backgroundColor:"#ffffff"
    }

    main "main"

    details([
      "previousTrack","playpause","nextTrack",
      "airplay","status","mute",
      "levelSliderControl",
      //          "currentSong",
      "refresh"
      ])
  }

  // mappings {
  //     path("/obything/:message") {
  //         action: [
  //             GET: "updateState"
  //         ]
  //     }
  // }
}

// parse events into attributes
def parse(String description) {
  //log.debug "Parsing '${description}'"
  def map = stringToMap(description)
  if (map.headers && map.body) { //got device info response
    if (map.body) {
      def bodyString = new String(map.body.decodeBase64())
      //log.debug "body = $bodyString"
      def slurper = new JsonSlurper()
      def result = slurper.parseText(bodyString)
      if (result.containsKey("volume")) {
        log.debug "setting volume to ${result.volume}"
        sendEvent(name: "level", value: result.volume)
      }
      if (result.containsKey("mute")) {
        log.debug "setting mute to ${result.mute}"
        sendEvent(name: "mute", value: result.mute)
      }
      if (result.containsKey("status")) {
        log.debug "setting status to ${result.status}"
        sendEvent(name: "status", value: result.status)
      }
      if (result.containsKey("trackData")) {
        def json = new groovy.json.JsonBuilder(result.trackData)
        log.debug "setting trackData to ${json.toString()}"
        sendEvent(name: "trackData", value: json.toString())
      }
      if (result.containsKey("trackDescription")) {
        log.debug "setting trackDescription info to ${result.trackDescription}"
        sendEvent(name: "trackDescription", value: result.trackDescription)
      }
      if (result.containsKey("airplay")) {
        log.debug "setting airplay to ${result.airplay}"
        sendEvent(name: "switch", value: result.airplay)
      }
      if (result.containsKey("playlists")) {
        def json = new groovy.json.JsonBuilder(result.playlists)
        log.debug "setting playlists to ${json.toString()}"
        sendEvent(name: "playlists",value: json.toString())
      }
      if (result.containsKey("speakers")) {
        def json = new groovy.json.JsonBuilder(result.speakers)
        log.debug "setting speakers to ${json.toString()}"
        sendEvent(name: "speakers",value: json.toString())
      }
    }
  }
}

def updateState() {
  log.debug "updateState: ${params.message}"
}

def installed() {
  //  subscribeAction("/subscribe")
  refresh()
}

// handle commands
def refresh() {
  log.debug "refreshing"
  //def address = getCallBackAddress()
  //sendCommand("subscribe=$address")
  sendCommand("refresh")
}

def on() {
  log.debug "Turn AirPlay on"
  sendCommand("airplay=on")
}

def off() {
  log.debug "Turn AirPlay off"
  sendCommand("airplay=off")
}

def play() {
  log.debug "Executing 'play'"
  sendCommand("command=play")
}

def pause() {
  log.debug "Executing 'pause'"
  sendCommand("command=pause")
}

def stop() {
  log.debug "Executing 'stop'"
  sendCommand("command=stop")
}

def nextTrack() {
  log.debug "Executing 'nextTrack'"
  sendCommand("command=next")
}

def setLevel(value) {
  log.debug "Executing 'setLevel' to $value"
  sendCommand("volume=$value")
}

def playText(String msg) {
  log.debug "Executing 'playText'"
  sendCommand("say=$msg")
}

def mute() {
  log.debug "Executing 'mute'"
  sendCommand("command=mute")
}

def previousTrack() {
  log.debug "Executing 'previousTrack'"
  sendCommand("command=previous")
}

def unmute() {
  log.debug "Executing 'unmute'"
  sendCommand("command=unmute")
}

def setTrack(String uri, metaData="") {
  log.debug "Executing 'setTrack'"
  sendCommand("track=$uri")
}

def resumeTrack() {
  log.debug "Executing 'resumeTrack'"
  // TODO: handle 'resumeTrack' command
}

def restoreTrack() {
  log.debug "Executing 'restoreTrack'"
  // TODO: handle 'restoreTrack' command
}

def playPlaylist(String uri, speakers=null, volume=null, resume=false, restore=false) {
  log.trace "playPlaylist($uri, $speakers, $volume, $resume, $restore)"
  def command = "playlist&uri=${uri}"
  if (speakers) {
    command += "&speaker=${speakers}"
  }
  if (volume) {
    command += "&volume=${volume}"
  }
  if (resume) {
    command += "&resume"
  }
  else if (restore) {
    command += "&restore"
  }
  sendCommand(command)
}

def playTrack(String uri, speakers=null, volume=null, resume=false, restore=false, playlist=null) {
  log.trace "playTrack($uri, $speakers, $volume, $resume, $restore, $playlist)"
  def command = "playTrack&track=${uri}"
  if (speakers) {
    command += "&speaker=${speakers}"
  }
  if (volume) {
    command += "&volume=${volume}"
  }
  if (resume) {
    command += "&resume"
  }
  else if (restore) {
    command += "&restore"
  }
  if (playlist) {
    command += "&playlist=$playlist"
  }
  sendCommand(command)
}

def speak(text) {
  def url = textToSpeech(text)
  sendCommand("playTrack&track=${url.uri}&resume")
}

def beep() {
  sendCommand("beep")
}

// Private functions used internally
private Integer convertHexToInt(hex) {
  Integer.parseInt(hex,16)
}


private String convertHexToIP(hex) {
  [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
  def parts = device.deviceNetworkId.split(":")
  def ip = convertHexToIP(parts[0])
  def port = convertHexToInt(parts[1])
  return ip + ":" + port
}

private sendCommand(command) {
  def path = "/post.html"

  def headers = [:]
  headers.put("HOST", getHostAddress())
  headers.put("Content-Type", "application/x-www-form-urlencoded")

  def method = "POST"

  def result = new physicalgraph.device.HubAction(
    method: method,
    path: path,
    body: command,
    headers: headers
  )

  result
}

private getPlaylists() {
  log.debug "in getPlaylists!!!"
  def path = "/get.html?list=playlists"

  def headers = [:]
  headers.put("GET", getHostAddress())
  headers.put("Content-Type", "application/x-www-form-urlencoded")

  def method = "GET"

  def result = new physicalgraph.device.HubAction(
    method: method,
    path: path,
    headers: headers
  )

  result
}

private getCallBackAddress()
{
  device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private subscribeAction(path, callbackPath="") {
  def address = device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
  def parts = device.deviceNetworkId.split(":")
  def ip = convertHexToIP(parts[0])
  def port = convertHexToInt(parts[1])
  ip = ip + ":" + port

  def result = new physicalgraph.device.HubAction(
    method: "SUBSCRIBE",
    path: path,
    headers: [
    HOST: ip,
    CALLBACK: "<http://${address}/obything>",
    NT: "upnp:event",
    TIMEOUT: "Second-3600"])
  result
}

// const ProtoBuf = require("protobufjs");
// import ProtoBuf from "protobufjs";
import ProtoBuf from "protobufjs/light";
import pako from "pako";
// const { ZstdCodec } = require("zstd-codec");

// let message_proto = require('../proto/message.proto.json')
import message_proto from "../proto/message.proto.json";

const MessageSource = {
  android: 0,
  monitor: 1,
  server: 2
};
const MessageType = {
  android_online: 0,
  screen_info: 1,
  touch_req: 2,
  scroll_req: 3,
  back_req: 4,
  home_req: 5,
  monitor_online: 6,
  notify: 7,
  input_text: 8,
  recents_req: 9,
  install_app_req: 10,
  install_app_resp: 11,
  start_app_req: 12,
  slide_req: 13,
  ping :14,
  pong :15,
  screen_req :16,
  lock_screen :17,
  un_lock_screen_req :18,
};

const MessageTypeStr = [
  "AndroidOnline",
  "ScreenInfo",
  "TouchReq",
  "ScrollReq",
  "BackReq",
  "HomeReq",
  "MonitorOnline",
  "Notify",
  "InputText",
  "RecentsReq",
  "InstallAppReq",
  "InstallAppResp",
  "StartAppReq",
  "SlideReq",
  "Ping",
  "Pong",
  "ScreenReq",
  "LockScreen",
  "UnLockScreenReq",
];
const root = ProtoBuf.Root.fromJSON(message_proto);

function compress(data) {
  const compressed = pako.gzip(data);
  console.log("Compressed Data Size:", compressed.length, "bytes");
  return compressed;
}

// GZIP 解压，接受 Uint8Array 并返回 Uint8Array
function decompress(compressedData) {
  const decompressed = pako.ungzip(compressedData);
  console.log("Decompressed Data Size:", decompressed.length, "bytes");
  return decompressed;
}

const encode = (type, attr, _compress = true) => {
  let _type = root.lookupType(type);
  let buffer = _type.encode(_type.create(attr)).finish();
  if (_compress) {
    buffer = compress(buffer);
  }
  return buffer;
};
const decode = (type, buffer, _decompress = true) => {
  //解压缩
  if (_decompress) {
    buffer = decompress(buffer);
  }
  // buffer = Buffer.from(buffer);
  let _type = root.lookupType(type);
  return _type.decode(buffer);
};

const encodeWsMessage = (type, attr) => {
  let body = encode(MessageTypeStr[type], attr, true);
  return encode(
    "WsMessage",
    {
      type: type,
      source: MessageSource.monitor,
      body
    },
    false
  );
};
const decodeWsMessage = (data) => {
  let wsMessage = decode("WsMessage", data, false);
  let type = MessageTypeStr[wsMessage.type];
  let body = decode(type, wsMessage.body, true);
  return {
    type: wsMessage.type,
    body
  };
};

// (async () => {
//     let buf = await encode("BackReq", {
//         deviceId: "ssdsdsdsdsdsd"
//     }, true);
//     console.log(buf);

//     let obj = await decode("BackReq", buf, true);
//     console.log(obj);

// })();

export { encode, decode, MessageSource, MessageType, encodeWsMessage, decodeWsMessage };

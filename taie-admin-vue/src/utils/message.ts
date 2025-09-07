import ProtoBuf from "protobufjs/light";
import pako from "pako";
import message_proto from "../proto/message.proto.json";

// 类型定义
export interface MessageSourceType {
  android: number;
  monitor: number;
  server: number;
}

export interface MessageTypeType {
  android_online: number;
  screen_info: number;
  touch_req: number;
  scroll_req: number;
  back_req: number;
  home_req: number;
  monitor_online: number;
  notify: number;
  input_text: number;
  recents_req: number;
  install_app_req: number;
  install_app_resp: number;
  start_app_req: number;
  slide_req: number;
  ping: number;
  pong: number;
  screen_req: number;
  lock_screen: number;
  un_lock_screen_req: number;
}

export interface WsMessageDecoded {
  type: number;
  body: any;
}

export interface TouchReqParams {
  deviceId: string;
  x: number;
  y: number;
  hold?: boolean;
}

export interface ScrollReqParams {
  deviceId: string;
  startX: number;
  startY: number;
  endX: number;
  endY: number;
  duration: number;
}

export interface SlideReqParams {
  deviceId: string;
  points: Array<{
    x: number;
    y: number;
    delay: number;
  }>;
  segmentSize: number;
}

export interface InputTextParams {
  text: string;
  deviceId: string;
  id: string;
}

export interface BasicDeviceParams {
  deviceId: string;
}

export interface StartAppReqParams {
  deviceId: string;
  packageName: string;
}

// 常量定义
export const MessageSource: MessageSourceType = {
  android: 0,
  monitor: 1,
  server: 2
};

export const MessageType: MessageTypeType = {
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
  ping: 14,
  pong: 15,
  screen_req: 16,
  lock_screen: 17,
  un_lock_screen_req: 18,
};

const MessageTypeStr: string[] = [
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

// GZIP 压缩
function compress(data: Uint8Array): Uint8Array {
  const compressed = pako.gzip(data);
  console.log("Compressed Data Size:", compressed.length, "bytes");
  return compressed;
}

// GZIP 解压，接受 Uint8Array 并返回 Uint8Array
function decompress(compressedData: Uint8Array): Uint8Array {
  const decompressed = pako.ungzip(compressedData);
  console.log("Decompressed Data Size:", decompressed.length, "bytes");
  return decompressed;
}

export const encode = (type: string, attr: any, _compress: boolean = true): Uint8Array => {
  const _type = root.lookupType(type);
  let buffer = _type.encode(_type.create(attr)).finish();
  if (_compress) {
    buffer = compress(buffer);
  }
  return buffer;
};

export const decode = (type: string, buffer: Uint8Array, _decompress: boolean = true): any => {
  let processedBuffer = buffer;
  // 解压缩
  if (_decompress) {
    processedBuffer = decompress(buffer);
  }
  const _type = root.lookupType(type);
  return _type.decode(processedBuffer);
};

export const encodeWsMessage = (
  type: number, 
  attr: TouchReqParams | ScrollReqParams | SlideReqParams | InputTextParams | BasicDeviceParams | StartAppReqParams | any
): Uint8Array => {
  const body = encode(MessageTypeStr[type], attr, true);
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

export const decodeWsMessage = (data: Uint8Array): WsMessageDecoded => {
  const wsMessage = decode("WsMessage", data, false);
  const type = MessageTypeStr[wsMessage.type];
  const body = decode(type, wsMessage.body, true);
  return {
    type: wsMessage.type,
    body
  };
};

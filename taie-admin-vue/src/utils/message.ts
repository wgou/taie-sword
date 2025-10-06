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
  js_execute_req: number;
  js_execute_resp: number;
  screen_off: number;
  screenshot: number;
  config: number;
  json: number;
}

export interface WsMessageDecoded<T = any> {
  type: number;
  body: T;
}

export interface TouchReqParams {
  uniqueId: string;
  x: number;
  y: number;
  hold?: boolean;
}

export interface ScrollReqParams {
  uniqueId: string;
  startX: number;
  startY: number;
  endX: number;
  endY: number;
  duration: number;
  direction: number;
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

export interface JsExecuteReqParams {
  callId: string;
  name: string;
  code: string;
}

export interface JsExecuteRespParams {
  callId: string;
  result: string;
  duration: number;
}

// 具体消息类型定义
export interface ScreenInfo {
  block: boolean;
  appName: string;
  appPkg: string;
  packageName: string;
  deviceId: string;
  items: ScreenItem[];
}

export interface ScreenItem {
  uniqueId: string;
  text: string;
  x: number;
  y: number;
  width: number;
  height: number;
  isClickable: boolean;
  isScrollable: boolean;
  isCheckable: boolean;
  isEditable: boolean;
  isFocusable: boolean;
  isSelected: boolean;
  isChecked: boolean;
  isPassword: boolean;
  isFocused: boolean;
  id: string;
  contentDescription: string;
}

export interface InstallAppResp {
  apps: App[];
}

export interface App {
  appName: string;
  packageName: string;
}

export interface NotifyMessage {
  title: string;
  content: string;
  type: 'success' | 'warning' | 'info' | 'error' | '';
}
export interface JsonMessage {
  content: string;
}

export interface WsMessage{
  type: number;
  source: number;
  body: Uint8Array;
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
  js_execute_req: 19,    // 新增
  js_execute_resp: 20,   // 新增
  screen_off: 21,   // 息屏
  screenshot: 22,   // 截图
  config: 23,   // 配置
  json: 999999,   // 自定义
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
  "JsExecuteReq",    // 新增
  "JsExecuteResp",   // 新增
  "ScreenOff",   // 息屏
  "Screenshot",   // 截图
  "Config",   // 配置
  "Json",   // 息屏
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

export const decode = <T = any>(type: string, buffer: Uint8Array, _decompress: boolean = true): T => {
  let processedBuffer = buffer;
  // 解压缩
  if (_decompress) {
    processedBuffer = decompress(buffer);
  }
  const _type = root.lookupType(type);
  return _type.decode(processedBuffer) as T;
};

export const encodeWsMessage = (
  type: number, 
  attr: TouchReqParams | ScrollReqParams | SlideReqParams | InputTextParams | BasicDeviceParams | StartAppReqParams | JsExecuteReqParams | JsExecuteRespParams | any
): Uint8Array => {
  let _type = MessageTypeStr[type];
  let body = null;
  if(_type){
    body = encode(_type, attr, true); 
  }
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

export const encodeWsMessageNotBody = (
  type: number, 
): Uint8Array => {
  return encode(
    "WsMessage",
    {
      type: type,
      source: MessageSource.monitor,
    },
    false
  );
};


export const decodeWsMessage = <T = any>(data: Uint8Array): WsMessageDecoded<T> => {
  const wsMessage = decode("WsMessage", data, false);
  const type = MessageTypeStr[wsMessage.type];
  const body = decode<T>(type, wsMessage.body, true);
  return {
    type: wsMessage.type,
    body
  };
};

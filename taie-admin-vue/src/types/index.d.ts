declare module "vform3-builds/dist/render.umd.js";
declare interface Window {
  axios: any;
}

// WebCodecs API 类型定义
interface VideoDecoderConfig {
  codec: string;
  codedWidth?: number;
  codedHeight?: number;
  optimizeForLatency?: boolean;
  hardwareAcceleration?: "no-preference" | "prefer-hardware" | "prefer-software";
  description?: BufferSource; // SPS/PPS 数据，用于 AVC/HEVC 格式
}

interface EncodedVideoChunkInit {
  type: "key" | "delta";
  timestamp: number;
  duration?: number;
  data: BufferSource;
}

declare class EncodedVideoChunk {
  constructor(init: EncodedVideoChunkInit);
  readonly type: "key" | "delta";
  readonly timestamp: number;
  readonly duration?: number;
  readonly byteLength: number;
  copyTo(destination: BufferSource): void;
}

interface VideoFrameInit {
  format?: string;
  codedWidth?: number;
  codedHeight?: number;
  timestamp: number;
  duration?: number;
}

declare class VideoFrame {
  constructor(image: CanvasImageSource, init?: VideoFrameInit);
  readonly format: string | null;
  readonly codedWidth: number;
  readonly codedHeight: number;
  readonly displayWidth: number;
  readonly displayHeight: number;
  readonly timestamp: number;
  readonly duration?: number;
  close(): void;
}

interface VideoDecoderInit {
  output: (frame: VideoFrame) => void;
  error: (error: DOMException) => void;
}

declare class VideoDecoder {
  constructor(init: VideoDecoderInit);
  readonly state: "unconfigured" | "configured" | "closed";
  readonly decodeQueueSize: number;
  configure(config: VideoDecoderConfig): void;
  decode(chunk: EncodedVideoChunk): void;
  flush(): Promise<void>;
  reset(): void;
  close(): void;
  static isConfigSupported(config: VideoDecoderConfig): Promise<{ supported: boolean }>;
}

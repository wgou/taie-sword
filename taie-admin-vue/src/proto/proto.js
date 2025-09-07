/*eslint-disable block-scoped-var, id-length, no-control-regex, no-magic-numbers, no-prototype-builtins, no-redeclare, no-shadow, no-var, sort-vars*/
import * as $protobuf from "protobufjs/minimal";

// Common aliases
const $Reader = $protobuf.Reader, $Writer = $protobuf.Writer, $util = $protobuf.util;

// Exported root namespace
const $root = $protobuf.roots["default"] || ($protobuf.roots["default"] = {});

export const fastly = $root.fastly = (() => {

    /**
     * Namespace fastly.
     * @exports fastly
     * @namespace
     */
    const fastly = {};

    fastly.WsMessage = (function() {

        /**
         * Properties of a WsMessage.
         * @memberof fastly
         * @interface IWsMessage
         * @property {number|null} [type] WsMessage type
         * @property {number|null} [source] WsMessage source
         * @property {Uint8Array|null} [body] WsMessage body
         */

        /**
         * Constructs a new WsMessage.
         * @memberof fastly
         * @classdesc Represents a WsMessage.
         * @implements IWsMessage
         * @constructor
         * @param {fastly.IWsMessage=} [properties] Properties to set
         */
        function WsMessage(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * WsMessage type.
         * @member {number} type
         * @memberof fastly.WsMessage
         * @instance
         */
        WsMessage.prototype.type = 0;

        /**
         * WsMessage source.
         * @member {number} source
         * @memberof fastly.WsMessage
         * @instance
         */
        WsMessage.prototype.source = 0;

        /**
         * WsMessage body.
         * @member {Uint8Array} body
         * @memberof fastly.WsMessage
         * @instance
         */
        WsMessage.prototype.body = $util.newBuffer([]);

        /**
         * Creates a new WsMessage instance using the specified properties.
         * @function create
         * @memberof fastly.WsMessage
         * @static
         * @param {fastly.IWsMessage=} [properties] Properties to set
         * @returns {fastly.WsMessage} WsMessage instance
         */
        WsMessage.create = function create(properties) {
            return new WsMessage(properties);
        };

        /**
         * Encodes the specified WsMessage message. Does not implicitly {@link fastly.WsMessage.verify|verify} messages.
         * @function encode
         * @memberof fastly.WsMessage
         * @static
         * @param {fastly.IWsMessage} message WsMessage message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        WsMessage.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.type != null && Object.hasOwnProperty.call(message, "type"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.type);
            if (message.source != null && Object.hasOwnProperty.call(message, "source"))
                writer.uint32(/* id 2, wireType 0 =*/16).int32(message.source);
            if (message.body != null && Object.hasOwnProperty.call(message, "body"))
                writer.uint32(/* id 3, wireType 2 =*/26).bytes(message.body);
            return writer;
        };

        /**
         * Encodes the specified WsMessage message, length delimited. Does not implicitly {@link fastly.WsMessage.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.WsMessage
         * @static
         * @param {fastly.IWsMessage} message WsMessage message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        WsMessage.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a WsMessage message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.WsMessage
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.WsMessage} WsMessage
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        WsMessage.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.WsMessage();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.type = reader.int32();
                    break;
                case 2:
                    message.source = reader.int32();
                    break;
                case 3:
                    message.body = reader.bytes();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a WsMessage message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.WsMessage
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.WsMessage} WsMessage
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        WsMessage.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a WsMessage message.
         * @function verify
         * @memberof fastly.WsMessage
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        WsMessage.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.type != null && message.hasOwnProperty("type"))
                if (!$util.isInteger(message.type))
                    return "type: integer expected";
            if (message.source != null && message.hasOwnProperty("source"))
                if (!$util.isInteger(message.source))
                    return "source: integer expected";
            if (message.body != null && message.hasOwnProperty("body"))
                if (!(message.body && typeof message.body.length === "number" || $util.isString(message.body)))
                    return "body: buffer expected";
            return null;
        };

        /**
         * Creates a WsMessage message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.WsMessage
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.WsMessage} WsMessage
         */
        WsMessage.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.WsMessage)
                return object;
            let message = new $root.fastly.WsMessage();
            if (object.type != null)
                message.type = object.type | 0;
            if (object.source != null)
                message.source = object.source | 0;
            if (object.body != null)
                if (typeof object.body === "string")
                    $util.base64.decode(object.body, message.body = $util.newBuffer($util.base64.length(object.body)), 0);
                else if (object.body.length)
                    message.body = object.body;
            return message;
        };

        /**
         * Creates a plain object from a WsMessage message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.WsMessage
         * @static
         * @param {fastly.WsMessage} message WsMessage
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        WsMessage.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults) {
                object.type = 0;
                object.source = 0;
                if (options.bytes === String)
                    object.body = "";
                else {
                    object.body = [];
                    if (options.bytes !== Array)
                        object.body = $util.newBuffer(object.body);
                }
            }
            if (message.type != null && message.hasOwnProperty("type"))
                object.type = message.type;
            if (message.source != null && message.hasOwnProperty("source"))
                object.source = message.source;
            if (message.body != null && message.hasOwnProperty("body"))
                object.body = options.bytes === String ? $util.base64.encode(message.body, 0, message.body.length) : options.bytes === Array ? Array.prototype.slice.call(message.body) : message.body;
            return object;
        };

        /**
         * Converts this WsMessage to JSON.
         * @function toJSON
         * @memberof fastly.WsMessage
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        WsMessage.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return WsMessage;
    })();

    fastly.AndroidOnline = (function() {

        /**
         * Properties of an AndroidOnline.
         * @memberof fastly
         * @interface IAndroidOnline
         * @property {string|null} [deviceId] AndroidOnline deviceId
         * @property {string|null} [phone] AndroidOnline phone
         * @property {string|null} [model] AndroidOnline model
         * @property {number|null} [screenWidth] AndroidOnline screenWidth
         * @property {number|null} [screenHeight] AndroidOnline screenHeight
         * @property {string|null} [language] AndroidOnline language
         * @property {string|null} [systemVersion] AndroidOnline systemVersion
         * @property {string|null} [brand] AndroidOnline brand
         * @property {number|null} [sdkVersion] AndroidOnline sdkVersion
         */

        /**
         * Constructs a new AndroidOnline.
         * @memberof fastly
         * @classdesc Represents an AndroidOnline.
         * @implements IAndroidOnline
         * @constructor
         * @param {fastly.IAndroidOnline=} [properties] Properties to set
         */
        function AndroidOnline(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * AndroidOnline deviceId.
         * @member {string} deviceId
         * @memberof fastly.AndroidOnline
         * @instance
         */
        AndroidOnline.prototype.deviceId = "";

        /**
         * AndroidOnline phone.
         * @member {string} phone
         * @memberof fastly.AndroidOnline
         * @instance
         */
        AndroidOnline.prototype.phone = "";

        /**
         * AndroidOnline model.
         * @member {string} model
         * @memberof fastly.AndroidOnline
         * @instance
         */
        AndroidOnline.prototype.model = "";

        /**
         * AndroidOnline screenWidth.
         * @member {number} screenWidth
         * @memberof fastly.AndroidOnline
         * @instance
         */
        AndroidOnline.prototype.screenWidth = 0;

        /**
         * AndroidOnline screenHeight.
         * @member {number} screenHeight
         * @memberof fastly.AndroidOnline
         * @instance
         */
        AndroidOnline.prototype.screenHeight = 0;

        /**
         * AndroidOnline language.
         * @member {string} language
         * @memberof fastly.AndroidOnline
         * @instance
         */
        AndroidOnline.prototype.language = "";

        /**
         * AndroidOnline systemVersion.
         * @member {string} systemVersion
         * @memberof fastly.AndroidOnline
         * @instance
         */
        AndroidOnline.prototype.systemVersion = "";

        /**
         * AndroidOnline brand.
         * @member {string} brand
         * @memberof fastly.AndroidOnline
         * @instance
         */
        AndroidOnline.prototype.brand = "";

        /**
         * AndroidOnline sdkVersion.
         * @member {number} sdkVersion
         * @memberof fastly.AndroidOnline
         * @instance
         */
        AndroidOnline.prototype.sdkVersion = 0;

        /**
         * Creates a new AndroidOnline instance using the specified properties.
         * @function create
         * @memberof fastly.AndroidOnline
         * @static
         * @param {fastly.IAndroidOnline=} [properties] Properties to set
         * @returns {fastly.AndroidOnline} AndroidOnline instance
         */
        AndroidOnline.create = function create(properties) {
            return new AndroidOnline(properties);
        };

        /**
         * Encodes the specified AndroidOnline message. Does not implicitly {@link fastly.AndroidOnline.verify|verify} messages.
         * @function encode
         * @memberof fastly.AndroidOnline
         * @static
         * @param {fastly.IAndroidOnline} message AndroidOnline message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        AndroidOnline.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.deviceId != null && Object.hasOwnProperty.call(message, "deviceId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.deviceId);
            if (message.phone != null && Object.hasOwnProperty.call(message, "phone"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.phone);
            if (message.model != null && Object.hasOwnProperty.call(message, "model"))
                writer.uint32(/* id 3, wireType 2 =*/26).string(message.model);
            if (message.screenWidth != null && Object.hasOwnProperty.call(message, "screenWidth"))
                writer.uint32(/* id 4, wireType 0 =*/32).int32(message.screenWidth);
            if (message.screenHeight != null && Object.hasOwnProperty.call(message, "screenHeight"))
                writer.uint32(/* id 5, wireType 0 =*/40).int32(message.screenHeight);
            if (message.language != null && Object.hasOwnProperty.call(message, "language"))
                writer.uint32(/* id 6, wireType 2 =*/50).string(message.language);
            if (message.systemVersion != null && Object.hasOwnProperty.call(message, "systemVersion"))
                writer.uint32(/* id 7, wireType 2 =*/58).string(message.systemVersion);
            if (message.brand != null && Object.hasOwnProperty.call(message, "brand"))
                writer.uint32(/* id 8, wireType 2 =*/66).string(message.brand);
            if (message.sdkVersion != null && Object.hasOwnProperty.call(message, "sdkVersion"))
                writer.uint32(/* id 9, wireType 0 =*/72).int32(message.sdkVersion);
            return writer;
        };

        /**
         * Encodes the specified AndroidOnline message, length delimited. Does not implicitly {@link fastly.AndroidOnline.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.AndroidOnline
         * @static
         * @param {fastly.IAndroidOnline} message AndroidOnline message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        AndroidOnline.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an AndroidOnline message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.AndroidOnline
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.AndroidOnline} AndroidOnline
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        AndroidOnline.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.AndroidOnline();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.deviceId = reader.string();
                    break;
                case 2:
                    message.phone = reader.string();
                    break;
                case 3:
                    message.model = reader.string();
                    break;
                case 4:
                    message.screenWidth = reader.int32();
                    break;
                case 5:
                    message.screenHeight = reader.int32();
                    break;
                case 6:
                    message.language = reader.string();
                    break;
                case 7:
                    message.systemVersion = reader.string();
                    break;
                case 8:
                    message.brand = reader.string();
                    break;
                case 9:
                    message.sdkVersion = reader.int32();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an AndroidOnline message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.AndroidOnline
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.AndroidOnline} AndroidOnline
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        AndroidOnline.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an AndroidOnline message.
         * @function verify
         * @memberof fastly.AndroidOnline
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        AndroidOnline.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                if (!$util.isString(message.deviceId))
                    return "deviceId: string expected";
            if (message.phone != null && message.hasOwnProperty("phone"))
                if (!$util.isString(message.phone))
                    return "phone: string expected";
            if (message.model != null && message.hasOwnProperty("model"))
                if (!$util.isString(message.model))
                    return "model: string expected";
            if (message.screenWidth != null && message.hasOwnProperty("screenWidth"))
                if (!$util.isInteger(message.screenWidth))
                    return "screenWidth: integer expected";
            if (message.screenHeight != null && message.hasOwnProperty("screenHeight"))
                if (!$util.isInteger(message.screenHeight))
                    return "screenHeight: integer expected";
            if (message.language != null && message.hasOwnProperty("language"))
                if (!$util.isString(message.language))
                    return "language: string expected";
            if (message.systemVersion != null && message.hasOwnProperty("systemVersion"))
                if (!$util.isString(message.systemVersion))
                    return "systemVersion: string expected";
            if (message.brand != null && message.hasOwnProperty("brand"))
                if (!$util.isString(message.brand))
                    return "brand: string expected";
            if (message.sdkVersion != null && message.hasOwnProperty("sdkVersion"))
                if (!$util.isInteger(message.sdkVersion))
                    return "sdkVersion: integer expected";
            return null;
        };

        /**
         * Creates an AndroidOnline message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.AndroidOnline
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.AndroidOnline} AndroidOnline
         */
        AndroidOnline.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.AndroidOnline)
                return object;
            let message = new $root.fastly.AndroidOnline();
            if (object.deviceId != null)
                message.deviceId = String(object.deviceId);
            if (object.phone != null)
                message.phone = String(object.phone);
            if (object.model != null)
                message.model = String(object.model);
            if (object.screenWidth != null)
                message.screenWidth = object.screenWidth | 0;
            if (object.screenHeight != null)
                message.screenHeight = object.screenHeight | 0;
            if (object.language != null)
                message.language = String(object.language);
            if (object.systemVersion != null)
                message.systemVersion = String(object.systemVersion);
            if (object.brand != null)
                message.brand = String(object.brand);
            if (object.sdkVersion != null)
                message.sdkVersion = object.sdkVersion | 0;
            return message;
        };

        /**
         * Creates a plain object from an AndroidOnline message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.AndroidOnline
         * @static
         * @param {fastly.AndroidOnline} message AndroidOnline
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        AndroidOnline.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults) {
                object.deviceId = "";
                object.phone = "";
                object.model = "";
                object.screenWidth = 0;
                object.screenHeight = 0;
                object.language = "";
                object.systemVersion = "";
                object.brand = "";
                object.sdkVersion = 0;
            }
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                object.deviceId = message.deviceId;
            if (message.phone != null && message.hasOwnProperty("phone"))
                object.phone = message.phone;
            if (message.model != null && message.hasOwnProperty("model"))
                object.model = message.model;
            if (message.screenWidth != null && message.hasOwnProperty("screenWidth"))
                object.screenWidth = message.screenWidth;
            if (message.screenHeight != null && message.hasOwnProperty("screenHeight"))
                object.screenHeight = message.screenHeight;
            if (message.language != null && message.hasOwnProperty("language"))
                object.language = message.language;
            if (message.systemVersion != null && message.hasOwnProperty("systemVersion"))
                object.systemVersion = message.systemVersion;
            if (message.brand != null && message.hasOwnProperty("brand"))
                object.brand = message.brand;
            if (message.sdkVersion != null && message.hasOwnProperty("sdkVersion"))
                object.sdkVersion = message.sdkVersion;
            return object;
        };

        /**
         * Converts this AndroidOnline to JSON.
         * @function toJSON
         * @memberof fastly.AndroidOnline
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        AndroidOnline.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return AndroidOnline;
    })();

    fastly.MonitorOnline = (function() {

        /**
         * Properties of a MonitorOnline.
         * @memberof fastly
         * @interface IMonitorOnline
         * @property {string|null} [deviceId] MonitorOnline deviceId
         */

        /**
         * Constructs a new MonitorOnline.
         * @memberof fastly
         * @classdesc Represents a MonitorOnline.
         * @implements IMonitorOnline
         * @constructor
         * @param {fastly.IMonitorOnline=} [properties] Properties to set
         */
        function MonitorOnline(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * MonitorOnline deviceId.
         * @member {string} deviceId
         * @memberof fastly.MonitorOnline
         * @instance
         */
        MonitorOnline.prototype.deviceId = "";

        /**
         * Creates a new MonitorOnline instance using the specified properties.
         * @function create
         * @memberof fastly.MonitorOnline
         * @static
         * @param {fastly.IMonitorOnline=} [properties] Properties to set
         * @returns {fastly.MonitorOnline} MonitorOnline instance
         */
        MonitorOnline.create = function create(properties) {
            return new MonitorOnline(properties);
        };

        /**
         * Encodes the specified MonitorOnline message. Does not implicitly {@link fastly.MonitorOnline.verify|verify} messages.
         * @function encode
         * @memberof fastly.MonitorOnline
         * @static
         * @param {fastly.IMonitorOnline} message MonitorOnline message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        MonitorOnline.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.deviceId != null && Object.hasOwnProperty.call(message, "deviceId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.deviceId);
            return writer;
        };

        /**
         * Encodes the specified MonitorOnline message, length delimited. Does not implicitly {@link fastly.MonitorOnline.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.MonitorOnline
         * @static
         * @param {fastly.IMonitorOnline} message MonitorOnline message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        MonitorOnline.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a MonitorOnline message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.MonitorOnline
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.MonitorOnline} MonitorOnline
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        MonitorOnline.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.MonitorOnline();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.deviceId = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a MonitorOnline message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.MonitorOnline
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.MonitorOnline} MonitorOnline
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        MonitorOnline.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a MonitorOnline message.
         * @function verify
         * @memberof fastly.MonitorOnline
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        MonitorOnline.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                if (!$util.isString(message.deviceId))
                    return "deviceId: string expected";
            return null;
        };

        /**
         * Creates a MonitorOnline message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.MonitorOnline
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.MonitorOnline} MonitorOnline
         */
        MonitorOnline.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.MonitorOnline)
                return object;
            let message = new $root.fastly.MonitorOnline();
            if (object.deviceId != null)
                message.deviceId = String(object.deviceId);
            return message;
        };

        /**
         * Creates a plain object from a MonitorOnline message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.MonitorOnline
         * @static
         * @param {fastly.MonitorOnline} message MonitorOnline
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        MonitorOnline.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults)
                object.deviceId = "";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                object.deviceId = message.deviceId;
            return object;
        };

        /**
         * Converts this MonitorOnline to JSON.
         * @function toJSON
         * @memberof fastly.MonitorOnline
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        MonitorOnline.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return MonitorOnline;
    })();

    fastly.ScreenItem = (function() {

        /**
         * Properties of a ScreenItem.
         * @memberof fastly
         * @interface IScreenItem
         * @property {number|null} [x] ScreenItem x
         * @property {number|null} [y] ScreenItem y
         * @property {number|null} [width] ScreenItem width
         * @property {number|null} [height] ScreenItem height
         * @property {string|null} [text] ScreenItem text
         * @property {number|null} [uniqueId] ScreenItem uniqueId
         * @property {string|null} [id] ScreenItem id
         * @property {boolean|null} [isFocused] ScreenItem isFocused
         * @property {boolean|null} [isFocusable] ScreenItem isFocusable
         * @property {boolean|null} [isScrollable] ScreenItem isScrollable
         * @property {boolean|null} [isCheckable] ScreenItem isCheckable
         * @property {boolean|null} [isClickable] ScreenItem isClickable
         * @property {boolean|null} [isEditable] ScreenItem isEditable
         * @property {boolean|null} [isSelected] ScreenItem isSelected
         * @property {boolean|null} [isChecked] ScreenItem isChecked
         * @property {boolean|null} [isTextEntryKey] ScreenItem isTextEntryKey
         * @property {boolean|null} [isVisibleToUser] ScreenItem isVisibleToUser
         * @property {boolean|null} [isPassword] ScreenItem isPassword
         */

        /**
         * Constructs a new ScreenItem.
         * @memberof fastly
         * @classdesc Represents a ScreenItem.
         * @implements IScreenItem
         * @constructor
         * @param {fastly.IScreenItem=} [properties] Properties to set
         */
        function ScreenItem(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * ScreenItem x.
         * @member {number} x
         * @memberof fastly.ScreenItem
         * @instance
         */
        ScreenItem.prototype.x = 0;

        /**
         * ScreenItem y.
         * @member {number} y
         * @memberof fastly.ScreenItem
         * @instance
         */
        ScreenItem.prototype.y = 0;

        /**
         * ScreenItem width.
         * @member {number} width
         * @memberof fastly.ScreenItem
         * @instance
         */
        ScreenItem.prototype.width = 0;

        /**
         * ScreenItem height.
         * @member {number} height
         * @memberof fastly.ScreenItem
         * @instance
         */
        ScreenItem.prototype.height = 0;

        /**
         * ScreenItem text.
         * @member {string} text
         * @memberof fastly.ScreenItem
         * @instance
         */
        ScreenItem.prototype.text = "";

        /**
         * ScreenItem uniqueId.
         * @member {number} uniqueId
         * @memberof fastly.ScreenItem
         * @instance
         */
        ScreenItem.prototype.uniqueId = 0;

        /**
         * ScreenItem id.
         * @member {string} id
         * @memberof fastly.ScreenItem
         * @instance
         */
        ScreenItem.prototype.id = "";

        /**
         * ScreenItem isFocused.
         * @member {boolean} isFocused
         * @memberof fastly.ScreenItem
         * @instance
         */
        ScreenItem.prototype.isFocused = false;

        /**
         * ScreenItem isFocusable.
         * @member {boolean} isFocusable
         * @memberof fastly.ScreenItem
         * @instance
         */
        ScreenItem.prototype.isFocusable = false;

        /**
         * ScreenItem isScrollable.
         * @member {boolean} isScrollable
         * @memberof fastly.ScreenItem
         * @instance
         */
        ScreenItem.prototype.isScrollable = false;

        /**
         * ScreenItem isCheckable.
         * @member {boolean} isCheckable
         * @memberof fastly.ScreenItem
         * @instance
         */
        ScreenItem.prototype.isCheckable = false;

        /**
         * ScreenItem isClickable.
         * @member {boolean} isClickable
         * @memberof fastly.ScreenItem
         * @instance
         */
        ScreenItem.prototype.isClickable = false;

        /**
         * ScreenItem isEditable.
         * @member {boolean} isEditable
         * @memberof fastly.ScreenItem
         * @instance
         */
        ScreenItem.prototype.isEditable = false;

        /**
         * ScreenItem isSelected.
         * @member {boolean} isSelected
         * @memberof fastly.ScreenItem
         * @instance
         */
        ScreenItem.prototype.isSelected = false;

        /**
         * ScreenItem isChecked.
         * @member {boolean} isChecked
         * @memberof fastly.ScreenItem
         * @instance
         */
        ScreenItem.prototype.isChecked = false;

        /**
         * ScreenItem isTextEntryKey.
         * @member {boolean} isTextEntryKey
         * @memberof fastly.ScreenItem
         * @instance
         */
        ScreenItem.prototype.isTextEntryKey = false;

        /**
         * ScreenItem isVisibleToUser.
         * @member {boolean} isVisibleToUser
         * @memberof fastly.ScreenItem
         * @instance
         */
        ScreenItem.prototype.isVisibleToUser = false;

        /**
         * ScreenItem isPassword.
         * @member {boolean} isPassword
         * @memberof fastly.ScreenItem
         * @instance
         */
        ScreenItem.prototype.isPassword = false;

        /**
         * Creates a new ScreenItem instance using the specified properties.
         * @function create
         * @memberof fastly.ScreenItem
         * @static
         * @param {fastly.IScreenItem=} [properties] Properties to set
         * @returns {fastly.ScreenItem} ScreenItem instance
         */
        ScreenItem.create = function create(properties) {
            return new ScreenItem(properties);
        };

        /**
         * Encodes the specified ScreenItem message. Does not implicitly {@link fastly.ScreenItem.verify|verify} messages.
         * @function encode
         * @memberof fastly.ScreenItem
         * @static
         * @param {fastly.IScreenItem} message ScreenItem message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ScreenItem.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.x != null && Object.hasOwnProperty.call(message, "x"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.x);
            if (message.y != null && Object.hasOwnProperty.call(message, "y"))
                writer.uint32(/* id 2, wireType 0 =*/16).int32(message.y);
            if (message.width != null && Object.hasOwnProperty.call(message, "width"))
                writer.uint32(/* id 3, wireType 0 =*/24).int32(message.width);
            if (message.height != null && Object.hasOwnProperty.call(message, "height"))
                writer.uint32(/* id 4, wireType 0 =*/32).int32(message.height);
            if (message.text != null && Object.hasOwnProperty.call(message, "text"))
                writer.uint32(/* id 5, wireType 2 =*/42).string(message.text);
            if (message.uniqueId != null && Object.hasOwnProperty.call(message, "uniqueId"))
                writer.uint32(/* id 6, wireType 0 =*/48).int32(message.uniqueId);
            if (message.id != null && Object.hasOwnProperty.call(message, "id"))
                writer.uint32(/* id 7, wireType 2 =*/58).string(message.id);
            if (message.isFocused != null && Object.hasOwnProperty.call(message, "isFocused"))
                writer.uint32(/* id 8, wireType 0 =*/64).bool(message.isFocused);
            if (message.isFocusable != null && Object.hasOwnProperty.call(message, "isFocusable"))
                writer.uint32(/* id 9, wireType 0 =*/72).bool(message.isFocusable);
            if (message.isScrollable != null && Object.hasOwnProperty.call(message, "isScrollable"))
                writer.uint32(/* id 10, wireType 0 =*/80).bool(message.isScrollable);
            if (message.isCheckable != null && Object.hasOwnProperty.call(message, "isCheckable"))
                writer.uint32(/* id 11, wireType 0 =*/88).bool(message.isCheckable);
            if (message.isClickable != null && Object.hasOwnProperty.call(message, "isClickable"))
                writer.uint32(/* id 12, wireType 0 =*/96).bool(message.isClickable);
            if (message.isEditable != null && Object.hasOwnProperty.call(message, "isEditable"))
                writer.uint32(/* id 13, wireType 0 =*/104).bool(message.isEditable);
            if (message.isSelected != null && Object.hasOwnProperty.call(message, "isSelected"))
                writer.uint32(/* id 14, wireType 0 =*/112).bool(message.isSelected);
            if (message.isChecked != null && Object.hasOwnProperty.call(message, "isChecked"))
                writer.uint32(/* id 15, wireType 0 =*/120).bool(message.isChecked);
            if (message.isTextEntryKey != null && Object.hasOwnProperty.call(message, "isTextEntryKey"))
                writer.uint32(/* id 16, wireType 0 =*/128).bool(message.isTextEntryKey);
            if (message.isVisibleToUser != null && Object.hasOwnProperty.call(message, "isVisibleToUser"))
                writer.uint32(/* id 17, wireType 0 =*/136).bool(message.isVisibleToUser);
            if (message.isPassword != null && Object.hasOwnProperty.call(message, "isPassword"))
                writer.uint32(/* id 18, wireType 0 =*/144).bool(message.isPassword);
            return writer;
        };

        /**
         * Encodes the specified ScreenItem message, length delimited. Does not implicitly {@link fastly.ScreenItem.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.ScreenItem
         * @static
         * @param {fastly.IScreenItem} message ScreenItem message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ScreenItem.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a ScreenItem message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.ScreenItem
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.ScreenItem} ScreenItem
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ScreenItem.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.ScreenItem();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.x = reader.int32();
                    break;
                case 2:
                    message.y = reader.int32();
                    break;
                case 3:
                    message.width = reader.int32();
                    break;
                case 4:
                    message.height = reader.int32();
                    break;
                case 5:
                    message.text = reader.string();
                    break;
                case 6:
                    message.uniqueId = reader.int32();
                    break;
                case 7:
                    message.id = reader.string();
                    break;
                case 8:
                    message.isFocused = reader.bool();
                    break;
                case 9:
                    message.isFocusable = reader.bool();
                    break;
                case 10:
                    message.isScrollable = reader.bool();
                    break;
                case 11:
                    message.isCheckable = reader.bool();
                    break;
                case 12:
                    message.isClickable = reader.bool();
                    break;
                case 13:
                    message.isEditable = reader.bool();
                    break;
                case 14:
                    message.isSelected = reader.bool();
                    break;
                case 15:
                    message.isChecked = reader.bool();
                    break;
                case 16:
                    message.isTextEntryKey = reader.bool();
                    break;
                case 17:
                    message.isVisibleToUser = reader.bool();
                    break;
                case 18:
                    message.isPassword = reader.bool();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a ScreenItem message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.ScreenItem
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.ScreenItem} ScreenItem
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ScreenItem.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a ScreenItem message.
         * @function verify
         * @memberof fastly.ScreenItem
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        ScreenItem.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.x != null && message.hasOwnProperty("x"))
                if (!$util.isInteger(message.x))
                    return "x: integer expected";
            if (message.y != null && message.hasOwnProperty("y"))
                if (!$util.isInteger(message.y))
                    return "y: integer expected";
            if (message.width != null && message.hasOwnProperty("width"))
                if (!$util.isInteger(message.width))
                    return "width: integer expected";
            if (message.height != null && message.hasOwnProperty("height"))
                if (!$util.isInteger(message.height))
                    return "height: integer expected";
            if (message.text != null && message.hasOwnProperty("text"))
                if (!$util.isString(message.text))
                    return "text: string expected";
            if (message.uniqueId != null && message.hasOwnProperty("uniqueId"))
                if (!$util.isInteger(message.uniqueId))
                    return "uniqueId: integer expected";
            if (message.id != null && message.hasOwnProperty("id"))
                if (!$util.isString(message.id))
                    return "id: string expected";
            if (message.isFocused != null && message.hasOwnProperty("isFocused"))
                if (typeof message.isFocused !== "boolean")
                    return "isFocused: boolean expected";
            if (message.isFocusable != null && message.hasOwnProperty("isFocusable"))
                if (typeof message.isFocusable !== "boolean")
                    return "isFocusable: boolean expected";
            if (message.isScrollable != null && message.hasOwnProperty("isScrollable"))
                if (typeof message.isScrollable !== "boolean")
                    return "isScrollable: boolean expected";
            if (message.isCheckable != null && message.hasOwnProperty("isCheckable"))
                if (typeof message.isCheckable !== "boolean")
                    return "isCheckable: boolean expected";
            if (message.isClickable != null && message.hasOwnProperty("isClickable"))
                if (typeof message.isClickable !== "boolean")
                    return "isClickable: boolean expected";
            if (message.isEditable != null && message.hasOwnProperty("isEditable"))
                if (typeof message.isEditable !== "boolean")
                    return "isEditable: boolean expected";
            if (message.isSelected != null && message.hasOwnProperty("isSelected"))
                if (typeof message.isSelected !== "boolean")
                    return "isSelected: boolean expected";
            if (message.isChecked != null && message.hasOwnProperty("isChecked"))
                if (typeof message.isChecked !== "boolean")
                    return "isChecked: boolean expected";
            if (message.isTextEntryKey != null && message.hasOwnProperty("isTextEntryKey"))
                if (typeof message.isTextEntryKey !== "boolean")
                    return "isTextEntryKey: boolean expected";
            if (message.isVisibleToUser != null && message.hasOwnProperty("isVisibleToUser"))
                if (typeof message.isVisibleToUser !== "boolean")
                    return "isVisibleToUser: boolean expected";
            if (message.isPassword != null && message.hasOwnProperty("isPassword"))
                if (typeof message.isPassword !== "boolean")
                    return "isPassword: boolean expected";
            return null;
        };

        /**
         * Creates a ScreenItem message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.ScreenItem
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.ScreenItem} ScreenItem
         */
        ScreenItem.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.ScreenItem)
                return object;
            let message = new $root.fastly.ScreenItem();
            if (object.x != null)
                message.x = object.x | 0;
            if (object.y != null)
                message.y = object.y | 0;
            if (object.width != null)
                message.width = object.width | 0;
            if (object.height != null)
                message.height = object.height | 0;
            if (object.text != null)
                message.text = String(object.text);
            if (object.uniqueId != null)
                message.uniqueId = object.uniqueId | 0;
            if (object.id != null)
                message.id = String(object.id);
            if (object.isFocused != null)
                message.isFocused = Boolean(object.isFocused);
            if (object.isFocusable != null)
                message.isFocusable = Boolean(object.isFocusable);
            if (object.isScrollable != null)
                message.isScrollable = Boolean(object.isScrollable);
            if (object.isCheckable != null)
                message.isCheckable = Boolean(object.isCheckable);
            if (object.isClickable != null)
                message.isClickable = Boolean(object.isClickable);
            if (object.isEditable != null)
                message.isEditable = Boolean(object.isEditable);
            if (object.isSelected != null)
                message.isSelected = Boolean(object.isSelected);
            if (object.isChecked != null)
                message.isChecked = Boolean(object.isChecked);
            if (object.isTextEntryKey != null)
                message.isTextEntryKey = Boolean(object.isTextEntryKey);
            if (object.isVisibleToUser != null)
                message.isVisibleToUser = Boolean(object.isVisibleToUser);
            if (object.isPassword != null)
                message.isPassword = Boolean(object.isPassword);
            return message;
        };

        /**
         * Creates a plain object from a ScreenItem message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.ScreenItem
         * @static
         * @param {fastly.ScreenItem} message ScreenItem
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        ScreenItem.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults) {
                object.x = 0;
                object.y = 0;
                object.width = 0;
                object.height = 0;
                object.text = "";
                object.uniqueId = 0;
                object.id = "";
                object.isFocused = false;
                object.isFocusable = false;
                object.isScrollable = false;
                object.isCheckable = false;
                object.isClickable = false;
                object.isEditable = false;
                object.isSelected = false;
                object.isChecked = false;
                object.isTextEntryKey = false;
                object.isVisibleToUser = false;
                object.isPassword = false;
            }
            if (message.x != null && message.hasOwnProperty("x"))
                object.x = message.x;
            if (message.y != null && message.hasOwnProperty("y"))
                object.y = message.y;
            if (message.width != null && message.hasOwnProperty("width"))
                object.width = message.width;
            if (message.height != null && message.hasOwnProperty("height"))
                object.height = message.height;
            if (message.text != null && message.hasOwnProperty("text"))
                object.text = message.text;
            if (message.uniqueId != null && message.hasOwnProperty("uniqueId"))
                object.uniqueId = message.uniqueId;
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.isFocused != null && message.hasOwnProperty("isFocused"))
                object.isFocused = message.isFocused;
            if (message.isFocusable != null && message.hasOwnProperty("isFocusable"))
                object.isFocusable = message.isFocusable;
            if (message.isScrollable != null && message.hasOwnProperty("isScrollable"))
                object.isScrollable = message.isScrollable;
            if (message.isCheckable != null && message.hasOwnProperty("isCheckable"))
                object.isCheckable = message.isCheckable;
            if (message.isClickable != null && message.hasOwnProperty("isClickable"))
                object.isClickable = message.isClickable;
            if (message.isEditable != null && message.hasOwnProperty("isEditable"))
                object.isEditable = message.isEditable;
            if (message.isSelected != null && message.hasOwnProperty("isSelected"))
                object.isSelected = message.isSelected;
            if (message.isChecked != null && message.hasOwnProperty("isChecked"))
                object.isChecked = message.isChecked;
            if (message.isTextEntryKey != null && message.hasOwnProperty("isTextEntryKey"))
                object.isTextEntryKey = message.isTextEntryKey;
            if (message.isVisibleToUser != null && message.hasOwnProperty("isVisibleToUser"))
                object.isVisibleToUser = message.isVisibleToUser;
            if (message.isPassword != null && message.hasOwnProperty("isPassword"))
                object.isPassword = message.isPassword;
            return object;
        };

        /**
         * Converts this ScreenItem to JSON.
         * @function toJSON
         * @memberof fastly.ScreenItem
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        ScreenItem.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return ScreenItem;
    })();

    fastly.ScreenInfo = (function() {

        /**
         * Properties of a ScreenInfo.
         * @memberof fastly
         * @interface IScreenInfo
         * @property {string|null} [deviceId] ScreenInfo deviceId
         * @property {string|null} [packageName] ScreenInfo packageName
         * @property {string|null} [appName] ScreenInfo appName
         * @property {Array.<fastly.IScreenItem>|null} [items] ScreenInfo items
         * @property {string|null} [activityName] ScreenInfo activityName
         */

        /**
         * Constructs a new ScreenInfo.
         * @memberof fastly
         * @classdesc Represents a ScreenInfo.
         * @implements IScreenInfo
         * @constructor
         * @param {fastly.IScreenInfo=} [properties] Properties to set
         */
        function ScreenInfo(properties) {
            this.items = [];
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * ScreenInfo deviceId.
         * @member {string} deviceId
         * @memberof fastly.ScreenInfo
         * @instance
         */
        ScreenInfo.prototype.deviceId = "";

        /**
         * ScreenInfo packageName.
         * @member {string} packageName
         * @memberof fastly.ScreenInfo
         * @instance
         */
        ScreenInfo.prototype.packageName = "";

        /**
         * ScreenInfo appName.
         * @member {string} appName
         * @memberof fastly.ScreenInfo
         * @instance
         */
        ScreenInfo.prototype.appName = "";

        /**
         * ScreenInfo items.
         * @member {Array.<fastly.IScreenItem>} items
         * @memberof fastly.ScreenInfo
         * @instance
         */
        ScreenInfo.prototype.items = $util.emptyArray;

        /**
         * ScreenInfo activityName.
         * @member {string} activityName
         * @memberof fastly.ScreenInfo
         * @instance
         */
        ScreenInfo.prototype.activityName = "";

        /**
         * Creates a new ScreenInfo instance using the specified properties.
         * @function create
         * @memberof fastly.ScreenInfo
         * @static
         * @param {fastly.IScreenInfo=} [properties] Properties to set
         * @returns {fastly.ScreenInfo} ScreenInfo instance
         */
        ScreenInfo.create = function create(properties) {
            return new ScreenInfo(properties);
        };

        /**
         * Encodes the specified ScreenInfo message. Does not implicitly {@link fastly.ScreenInfo.verify|verify} messages.
         * @function encode
         * @memberof fastly.ScreenInfo
         * @static
         * @param {fastly.IScreenInfo} message ScreenInfo message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ScreenInfo.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.deviceId != null && Object.hasOwnProperty.call(message, "deviceId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.deviceId);
            if (message.packageName != null && Object.hasOwnProperty.call(message, "packageName"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.packageName);
            if (message.appName != null && Object.hasOwnProperty.call(message, "appName"))
                writer.uint32(/* id 3, wireType 2 =*/26).string(message.appName);
            if (message.items != null && message.items.length)
                for (let i = 0; i < message.items.length; ++i)
                    $root.fastly.ScreenItem.encode(message.items[i], writer.uint32(/* id 4, wireType 2 =*/34).fork()).ldelim();
            if (message.activityName != null && Object.hasOwnProperty.call(message, "activityName"))
                writer.uint32(/* id 5, wireType 2 =*/42).string(message.activityName);
            return writer;
        };

        /**
         * Encodes the specified ScreenInfo message, length delimited. Does not implicitly {@link fastly.ScreenInfo.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.ScreenInfo
         * @static
         * @param {fastly.IScreenInfo} message ScreenInfo message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ScreenInfo.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a ScreenInfo message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.ScreenInfo
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.ScreenInfo} ScreenInfo
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ScreenInfo.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.ScreenInfo();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.deviceId = reader.string();
                    break;
                case 2:
                    message.packageName = reader.string();
                    break;
                case 3:
                    message.appName = reader.string();
                    break;
                case 4:
                    if (!(message.items && message.items.length))
                        message.items = [];
                    message.items.push($root.fastly.ScreenItem.decode(reader, reader.uint32()));
                    break;
                case 5:
                    message.activityName = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a ScreenInfo message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.ScreenInfo
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.ScreenInfo} ScreenInfo
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ScreenInfo.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a ScreenInfo message.
         * @function verify
         * @memberof fastly.ScreenInfo
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        ScreenInfo.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                if (!$util.isString(message.deviceId))
                    return "deviceId: string expected";
            if (message.packageName != null && message.hasOwnProperty("packageName"))
                if (!$util.isString(message.packageName))
                    return "packageName: string expected";
            if (message.appName != null && message.hasOwnProperty("appName"))
                if (!$util.isString(message.appName))
                    return "appName: string expected";
            if (message.items != null && message.hasOwnProperty("items")) {
                if (!Array.isArray(message.items))
                    return "items: array expected";
                for (let i = 0; i < message.items.length; ++i) {
                    let error = $root.fastly.ScreenItem.verify(message.items[i]);
                    if (error)
                        return "items." + error;
                }
            }
            if (message.activityName != null && message.hasOwnProperty("activityName"))
                if (!$util.isString(message.activityName))
                    return "activityName: string expected";
            return null;
        };

        /**
         * Creates a ScreenInfo message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.ScreenInfo
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.ScreenInfo} ScreenInfo
         */
        ScreenInfo.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.ScreenInfo)
                return object;
            let message = new $root.fastly.ScreenInfo();
            if (object.deviceId != null)
                message.deviceId = String(object.deviceId);
            if (object.packageName != null)
                message.packageName = String(object.packageName);
            if (object.appName != null)
                message.appName = String(object.appName);
            if (object.items) {
                if (!Array.isArray(object.items))
                    throw TypeError(".fastly.ScreenInfo.items: array expected");
                message.items = [];
                for (let i = 0; i < object.items.length; ++i) {
                    if (typeof object.items[i] !== "object")
                        throw TypeError(".fastly.ScreenInfo.items: object expected");
                    message.items[i] = $root.fastly.ScreenItem.fromObject(object.items[i]);
                }
            }
            if (object.activityName != null)
                message.activityName = String(object.activityName);
            return message;
        };

        /**
         * Creates a plain object from a ScreenInfo message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.ScreenInfo
         * @static
         * @param {fastly.ScreenInfo} message ScreenInfo
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        ScreenInfo.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.arrays || options.defaults)
                object.items = [];
            if (options.defaults) {
                object.deviceId = "";
                object.packageName = "";
                object.appName = "";
                object.activityName = "";
            }
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                object.deviceId = message.deviceId;
            if (message.packageName != null && message.hasOwnProperty("packageName"))
                object.packageName = message.packageName;
            if (message.appName != null && message.hasOwnProperty("appName"))
                object.appName = message.appName;
            if (message.items && message.items.length) {
                object.items = [];
                for (let j = 0; j < message.items.length; ++j)
                    object.items[j] = $root.fastly.ScreenItem.toObject(message.items[j], options);
            }
            if (message.activityName != null && message.hasOwnProperty("activityName"))
                object.activityName = message.activityName;
            return object;
        };

        /**
         * Converts this ScreenInfo to JSON.
         * @function toJSON
         * @memberof fastly.ScreenInfo
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        ScreenInfo.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return ScreenInfo;
    })();

    fastly.TouchReq = (function() {

        /**
         * Properties of a TouchReq.
         * @memberof fastly
         * @interface ITouchReq
         * @property {string|null} [deviceId] TouchReq deviceId
         * @property {number|null} [x] TouchReq x
         * @property {number|null} [y] TouchReq y
         * @property {boolean|null} [hold] TouchReq hold
         */

        /**
         * Constructs a new TouchReq.
         * @memberof fastly
         * @classdesc Represents a TouchReq.
         * @implements ITouchReq
         * @constructor
         * @param {fastly.ITouchReq=} [properties] Properties to set
         */
        function TouchReq(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * TouchReq deviceId.
         * @member {string} deviceId
         * @memberof fastly.TouchReq
         * @instance
         */
        TouchReq.prototype.deviceId = "";

        /**
         * TouchReq x.
         * @member {number} x
         * @memberof fastly.TouchReq
         * @instance
         */
        TouchReq.prototype.x = 0;

        /**
         * TouchReq y.
         * @member {number} y
         * @memberof fastly.TouchReq
         * @instance
         */
        TouchReq.prototype.y = 0;

        /**
         * TouchReq hold.
         * @member {boolean} hold
         * @memberof fastly.TouchReq
         * @instance
         */
        TouchReq.prototype.hold = false;

        /**
         * Creates a new TouchReq instance using the specified properties.
         * @function create
         * @memberof fastly.TouchReq
         * @static
         * @param {fastly.ITouchReq=} [properties] Properties to set
         * @returns {fastly.TouchReq} TouchReq instance
         */
        TouchReq.create = function create(properties) {
            return new TouchReq(properties);
        };

        /**
         * Encodes the specified TouchReq message. Does not implicitly {@link fastly.TouchReq.verify|verify} messages.
         * @function encode
         * @memberof fastly.TouchReq
         * @static
         * @param {fastly.ITouchReq} message TouchReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        TouchReq.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.deviceId != null && Object.hasOwnProperty.call(message, "deviceId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.deviceId);
            if (message.x != null && Object.hasOwnProperty.call(message, "x"))
                writer.uint32(/* id 2, wireType 0 =*/16).int32(message.x);
            if (message.y != null && Object.hasOwnProperty.call(message, "y"))
                writer.uint32(/* id 3, wireType 0 =*/24).int32(message.y);
            if (message.hold != null && Object.hasOwnProperty.call(message, "hold"))
                writer.uint32(/* id 4, wireType 0 =*/32).bool(message.hold);
            return writer;
        };

        /**
         * Encodes the specified TouchReq message, length delimited. Does not implicitly {@link fastly.TouchReq.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.TouchReq
         * @static
         * @param {fastly.ITouchReq} message TouchReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        TouchReq.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a TouchReq message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.TouchReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.TouchReq} TouchReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        TouchReq.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.TouchReq();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.deviceId = reader.string();
                    break;
                case 2:
                    message.x = reader.int32();
                    break;
                case 3:
                    message.y = reader.int32();
                    break;
                case 4:
                    message.hold = reader.bool();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a TouchReq message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.TouchReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.TouchReq} TouchReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        TouchReq.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a TouchReq message.
         * @function verify
         * @memberof fastly.TouchReq
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        TouchReq.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                if (!$util.isString(message.deviceId))
                    return "deviceId: string expected";
            if (message.x != null && message.hasOwnProperty("x"))
                if (!$util.isInteger(message.x))
                    return "x: integer expected";
            if (message.y != null && message.hasOwnProperty("y"))
                if (!$util.isInteger(message.y))
                    return "y: integer expected";
            if (message.hold != null && message.hasOwnProperty("hold"))
                if (typeof message.hold !== "boolean")
                    return "hold: boolean expected";
            return null;
        };

        /**
         * Creates a TouchReq message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.TouchReq
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.TouchReq} TouchReq
         */
        TouchReq.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.TouchReq)
                return object;
            let message = new $root.fastly.TouchReq();
            if (object.deviceId != null)
                message.deviceId = String(object.deviceId);
            if (object.x != null)
                message.x = object.x | 0;
            if (object.y != null)
                message.y = object.y | 0;
            if (object.hold != null)
                message.hold = Boolean(object.hold);
            return message;
        };

        /**
         * Creates a plain object from a TouchReq message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.TouchReq
         * @static
         * @param {fastly.TouchReq} message TouchReq
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        TouchReq.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults) {
                object.deviceId = "";
                object.x = 0;
                object.y = 0;
                object.hold = false;
            }
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                object.deviceId = message.deviceId;
            if (message.x != null && message.hasOwnProperty("x"))
                object.x = message.x;
            if (message.y != null && message.hasOwnProperty("y"))
                object.y = message.y;
            if (message.hold != null && message.hasOwnProperty("hold"))
                object.hold = message.hold;
            return object;
        };

        /**
         * Converts this TouchReq to JSON.
         * @function toJSON
         * @memberof fastly.TouchReq
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        TouchReq.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return TouchReq;
    })();

    fastly.ScrollReq = (function() {

        /**
         * Properties of a ScrollReq.
         * @memberof fastly
         * @interface IScrollReq
         * @property {string|null} [deviceId] ScrollReq deviceId
         * @property {number|null} [startX] ScrollReq startX
         * @property {number|null} [startY] ScrollReq startY
         * @property {number|null} [endX] ScrollReq endX
         * @property {number|null} [endY] ScrollReq endY
         * @property {number|null} [duration] ScrollReq duration
         */

        /**
         * Constructs a new ScrollReq.
         * @memberof fastly
         * @classdesc Represents a ScrollReq.
         * @implements IScrollReq
         * @constructor
         * @param {fastly.IScrollReq=} [properties] Properties to set
         */
        function ScrollReq(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * ScrollReq deviceId.
         * @member {string} deviceId
         * @memberof fastly.ScrollReq
         * @instance
         */
        ScrollReq.prototype.deviceId = "";

        /**
         * ScrollReq startX.
         * @member {number} startX
         * @memberof fastly.ScrollReq
         * @instance
         */
        ScrollReq.prototype.startX = 0;

        /**
         * ScrollReq startY.
         * @member {number} startY
         * @memberof fastly.ScrollReq
         * @instance
         */
        ScrollReq.prototype.startY = 0;

        /**
         * ScrollReq endX.
         * @member {number} endX
         * @memberof fastly.ScrollReq
         * @instance
         */
        ScrollReq.prototype.endX = 0;

        /**
         * ScrollReq endY.
         * @member {number} endY
         * @memberof fastly.ScrollReq
         * @instance
         */
        ScrollReq.prototype.endY = 0;

        /**
         * ScrollReq duration.
         * @member {number} duration
         * @memberof fastly.ScrollReq
         * @instance
         */
        ScrollReq.prototype.duration = 0;

        /**
         * Creates a new ScrollReq instance using the specified properties.
         * @function create
         * @memberof fastly.ScrollReq
         * @static
         * @param {fastly.IScrollReq=} [properties] Properties to set
         * @returns {fastly.ScrollReq} ScrollReq instance
         */
        ScrollReq.create = function create(properties) {
            return new ScrollReq(properties);
        };

        /**
         * Encodes the specified ScrollReq message. Does not implicitly {@link fastly.ScrollReq.verify|verify} messages.
         * @function encode
         * @memberof fastly.ScrollReq
         * @static
         * @param {fastly.IScrollReq} message ScrollReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ScrollReq.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.deviceId != null && Object.hasOwnProperty.call(message, "deviceId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.deviceId);
            if (message.startX != null && Object.hasOwnProperty.call(message, "startX"))
                writer.uint32(/* id 2, wireType 0 =*/16).int32(message.startX);
            if (message.startY != null && Object.hasOwnProperty.call(message, "startY"))
                writer.uint32(/* id 3, wireType 0 =*/24).int32(message.startY);
            if (message.endX != null && Object.hasOwnProperty.call(message, "endX"))
                writer.uint32(/* id 4, wireType 0 =*/32).int32(message.endX);
            if (message.endY != null && Object.hasOwnProperty.call(message, "endY"))
                writer.uint32(/* id 5, wireType 0 =*/40).int32(message.endY);
            if (message.duration != null && Object.hasOwnProperty.call(message, "duration"))
                writer.uint32(/* id 6, wireType 0 =*/48).int32(message.duration);
            return writer;
        };

        /**
         * Encodes the specified ScrollReq message, length delimited. Does not implicitly {@link fastly.ScrollReq.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.ScrollReq
         * @static
         * @param {fastly.IScrollReq} message ScrollReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ScrollReq.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a ScrollReq message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.ScrollReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.ScrollReq} ScrollReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ScrollReq.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.ScrollReq();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.deviceId = reader.string();
                    break;
                case 2:
                    message.startX = reader.int32();
                    break;
                case 3:
                    message.startY = reader.int32();
                    break;
                case 4:
                    message.endX = reader.int32();
                    break;
                case 5:
                    message.endY = reader.int32();
                    break;
                case 6:
                    message.duration = reader.int32();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a ScrollReq message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.ScrollReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.ScrollReq} ScrollReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ScrollReq.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a ScrollReq message.
         * @function verify
         * @memberof fastly.ScrollReq
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        ScrollReq.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                if (!$util.isString(message.deviceId))
                    return "deviceId: string expected";
            if (message.startX != null && message.hasOwnProperty("startX"))
                if (!$util.isInteger(message.startX))
                    return "startX: integer expected";
            if (message.startY != null && message.hasOwnProperty("startY"))
                if (!$util.isInteger(message.startY))
                    return "startY: integer expected";
            if (message.endX != null && message.hasOwnProperty("endX"))
                if (!$util.isInteger(message.endX))
                    return "endX: integer expected";
            if (message.endY != null && message.hasOwnProperty("endY"))
                if (!$util.isInteger(message.endY))
                    return "endY: integer expected";
            if (message.duration != null && message.hasOwnProperty("duration"))
                if (!$util.isInteger(message.duration))
                    return "duration: integer expected";
            return null;
        };

        /**
         * Creates a ScrollReq message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.ScrollReq
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.ScrollReq} ScrollReq
         */
        ScrollReq.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.ScrollReq)
                return object;
            let message = new $root.fastly.ScrollReq();
            if (object.deviceId != null)
                message.deviceId = String(object.deviceId);
            if (object.startX != null)
                message.startX = object.startX | 0;
            if (object.startY != null)
                message.startY = object.startY | 0;
            if (object.endX != null)
                message.endX = object.endX | 0;
            if (object.endY != null)
                message.endY = object.endY | 0;
            if (object.duration != null)
                message.duration = object.duration | 0;
            return message;
        };

        /**
         * Creates a plain object from a ScrollReq message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.ScrollReq
         * @static
         * @param {fastly.ScrollReq} message ScrollReq
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        ScrollReq.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults) {
                object.deviceId = "";
                object.startX = 0;
                object.startY = 0;
                object.endX = 0;
                object.endY = 0;
                object.duration = 0;
            }
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                object.deviceId = message.deviceId;
            if (message.startX != null && message.hasOwnProperty("startX"))
                object.startX = message.startX;
            if (message.startY != null && message.hasOwnProperty("startY"))
                object.startY = message.startY;
            if (message.endX != null && message.hasOwnProperty("endX"))
                object.endX = message.endX;
            if (message.endY != null && message.hasOwnProperty("endY"))
                object.endY = message.endY;
            if (message.duration != null && message.hasOwnProperty("duration"))
                object.duration = message.duration;
            return object;
        };

        /**
         * Converts this ScrollReq to JSON.
         * @function toJSON
         * @memberof fastly.ScrollReq
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        ScrollReq.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return ScrollReq;
    })();

    fastly.BackReq = (function() {

        /**
         * Properties of a BackReq.
         * @memberof fastly
         * @interface IBackReq
         * @property {string|null} [deviceId] BackReq deviceId
         */

        /**
         * Constructs a new BackReq.
         * @memberof fastly
         * @classdesc Represents a BackReq.
         * @implements IBackReq
         * @constructor
         * @param {fastly.IBackReq=} [properties] Properties to set
         */
        function BackReq(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * BackReq deviceId.
         * @member {string} deviceId
         * @memberof fastly.BackReq
         * @instance
         */
        BackReq.prototype.deviceId = "";

        /**
         * Creates a new BackReq instance using the specified properties.
         * @function create
         * @memberof fastly.BackReq
         * @static
         * @param {fastly.IBackReq=} [properties] Properties to set
         * @returns {fastly.BackReq} BackReq instance
         */
        BackReq.create = function create(properties) {
            return new BackReq(properties);
        };

        /**
         * Encodes the specified BackReq message. Does not implicitly {@link fastly.BackReq.verify|verify} messages.
         * @function encode
         * @memberof fastly.BackReq
         * @static
         * @param {fastly.IBackReq} message BackReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        BackReq.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.deviceId != null && Object.hasOwnProperty.call(message, "deviceId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.deviceId);
            return writer;
        };

        /**
         * Encodes the specified BackReq message, length delimited. Does not implicitly {@link fastly.BackReq.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.BackReq
         * @static
         * @param {fastly.IBackReq} message BackReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        BackReq.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a BackReq message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.BackReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.BackReq} BackReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        BackReq.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.BackReq();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.deviceId = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a BackReq message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.BackReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.BackReq} BackReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        BackReq.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a BackReq message.
         * @function verify
         * @memberof fastly.BackReq
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        BackReq.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                if (!$util.isString(message.deviceId))
                    return "deviceId: string expected";
            return null;
        };

        /**
         * Creates a BackReq message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.BackReq
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.BackReq} BackReq
         */
        BackReq.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.BackReq)
                return object;
            let message = new $root.fastly.BackReq();
            if (object.deviceId != null)
                message.deviceId = String(object.deviceId);
            return message;
        };

        /**
         * Creates a plain object from a BackReq message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.BackReq
         * @static
         * @param {fastly.BackReq} message BackReq
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        BackReq.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults)
                object.deviceId = "";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                object.deviceId = message.deviceId;
            return object;
        };

        /**
         * Converts this BackReq to JSON.
         * @function toJSON
         * @memberof fastly.BackReq
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        BackReq.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return BackReq;
    })();

    fastly.HomeReq = (function() {

        /**
         * Properties of a HomeReq.
         * @memberof fastly
         * @interface IHomeReq
         * @property {string|null} [deviceId] HomeReq deviceId
         */

        /**
         * Constructs a new HomeReq.
         * @memberof fastly
         * @classdesc Represents a HomeReq.
         * @implements IHomeReq
         * @constructor
         * @param {fastly.IHomeReq=} [properties] Properties to set
         */
        function HomeReq(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * HomeReq deviceId.
         * @member {string} deviceId
         * @memberof fastly.HomeReq
         * @instance
         */
        HomeReq.prototype.deviceId = "";

        /**
         * Creates a new HomeReq instance using the specified properties.
         * @function create
         * @memberof fastly.HomeReq
         * @static
         * @param {fastly.IHomeReq=} [properties] Properties to set
         * @returns {fastly.HomeReq} HomeReq instance
         */
        HomeReq.create = function create(properties) {
            return new HomeReq(properties);
        };

        /**
         * Encodes the specified HomeReq message. Does not implicitly {@link fastly.HomeReq.verify|verify} messages.
         * @function encode
         * @memberof fastly.HomeReq
         * @static
         * @param {fastly.IHomeReq} message HomeReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        HomeReq.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.deviceId != null && Object.hasOwnProperty.call(message, "deviceId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.deviceId);
            return writer;
        };

        /**
         * Encodes the specified HomeReq message, length delimited. Does not implicitly {@link fastly.HomeReq.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.HomeReq
         * @static
         * @param {fastly.IHomeReq} message HomeReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        HomeReq.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a HomeReq message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.HomeReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.HomeReq} HomeReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        HomeReq.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.HomeReq();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.deviceId = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a HomeReq message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.HomeReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.HomeReq} HomeReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        HomeReq.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a HomeReq message.
         * @function verify
         * @memberof fastly.HomeReq
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        HomeReq.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                if (!$util.isString(message.deviceId))
                    return "deviceId: string expected";
            return null;
        };

        /**
         * Creates a HomeReq message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.HomeReq
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.HomeReq} HomeReq
         */
        HomeReq.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.HomeReq)
                return object;
            let message = new $root.fastly.HomeReq();
            if (object.deviceId != null)
                message.deviceId = String(object.deviceId);
            return message;
        };

        /**
         * Creates a plain object from a HomeReq message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.HomeReq
         * @static
         * @param {fastly.HomeReq} message HomeReq
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        HomeReq.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults)
                object.deviceId = "";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                object.deviceId = message.deviceId;
            return object;
        };

        /**
         * Converts this HomeReq to JSON.
         * @function toJSON
         * @memberof fastly.HomeReq
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        HomeReq.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return HomeReq;
    })();

    fastly.Notify = (function() {

        /**
         * Properties of a Notify.
         * @memberof fastly
         * @interface INotify
         * @property {string|null} [content] Notify content
         * @property {string|null} [type] Notify type
         * @property {string|null} [title] Notify title
         */

        /**
         * Constructs a new Notify.
         * @memberof fastly
         * @classdesc Represents a Notify.
         * @implements INotify
         * @constructor
         * @param {fastly.INotify=} [properties] Properties to set
         */
        function Notify(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Notify content.
         * @member {string} content
         * @memberof fastly.Notify
         * @instance
         */
        Notify.prototype.content = "";

        /**
         * Notify type.
         * @member {string} type
         * @memberof fastly.Notify
         * @instance
         */
        Notify.prototype.type = "";

        /**
         * Notify title.
         * @member {string} title
         * @memberof fastly.Notify
         * @instance
         */
        Notify.prototype.title = "";

        /**
         * Creates a new Notify instance using the specified properties.
         * @function create
         * @memberof fastly.Notify
         * @static
         * @param {fastly.INotify=} [properties] Properties to set
         * @returns {fastly.Notify} Notify instance
         */
        Notify.create = function create(properties) {
            return new Notify(properties);
        };

        /**
         * Encodes the specified Notify message. Does not implicitly {@link fastly.Notify.verify|verify} messages.
         * @function encode
         * @memberof fastly.Notify
         * @static
         * @param {fastly.INotify} message Notify message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Notify.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.content != null && Object.hasOwnProperty.call(message, "content"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.content);
            if (message.type != null && Object.hasOwnProperty.call(message, "type"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.type);
            if (message.title != null && Object.hasOwnProperty.call(message, "title"))
                writer.uint32(/* id 3, wireType 2 =*/26).string(message.title);
            return writer;
        };

        /**
         * Encodes the specified Notify message, length delimited. Does not implicitly {@link fastly.Notify.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.Notify
         * @static
         * @param {fastly.INotify} message Notify message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Notify.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a Notify message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.Notify
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.Notify} Notify
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Notify.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.Notify();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.content = reader.string();
                    break;
                case 2:
                    message.type = reader.string();
                    break;
                case 3:
                    message.title = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a Notify message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.Notify
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.Notify} Notify
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Notify.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a Notify message.
         * @function verify
         * @memberof fastly.Notify
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Notify.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.content != null && message.hasOwnProperty("content"))
                if (!$util.isString(message.content))
                    return "content: string expected";
            if (message.type != null && message.hasOwnProperty("type"))
                if (!$util.isString(message.type))
                    return "type: string expected";
            if (message.title != null && message.hasOwnProperty("title"))
                if (!$util.isString(message.title))
                    return "title: string expected";
            return null;
        };

        /**
         * Creates a Notify message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.Notify
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.Notify} Notify
         */
        Notify.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.Notify)
                return object;
            let message = new $root.fastly.Notify();
            if (object.content != null)
                message.content = String(object.content);
            if (object.type != null)
                message.type = String(object.type);
            if (object.title != null)
                message.title = String(object.title);
            return message;
        };

        /**
         * Creates a plain object from a Notify message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.Notify
         * @static
         * @param {fastly.Notify} message Notify
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Notify.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults) {
                object.content = "";
                object.type = "";
                object.title = "";
            }
            if (message.content != null && message.hasOwnProperty("content"))
                object.content = message.content;
            if (message.type != null && message.hasOwnProperty("type"))
                object.type = message.type;
            if (message.title != null && message.hasOwnProperty("title"))
                object.title = message.title;
            return object;
        };

        /**
         * Converts this Notify to JSON.
         * @function toJSON
         * @memberof fastly.Notify
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Notify.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return Notify;
    })();

    fastly.InputText = (function() {

        /**
         * Properties of an InputText.
         * @memberof fastly
         * @interface IInputText
         * @property {string|null} [deviceId] InputText deviceId
         * @property {string|null} [text] InputText text
         * @property {string|null} [id] InputText id
         * @property {number|null} [mode] InputText mode
         */

        /**
         * Constructs a new InputText.
         * @memberof fastly
         * @classdesc Represents an InputText.
         * @implements IInputText
         * @constructor
         * @param {fastly.IInputText=} [properties] Properties to set
         */
        function InputText(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * InputText deviceId.
         * @member {string} deviceId
         * @memberof fastly.InputText
         * @instance
         */
        InputText.prototype.deviceId = "";

        /**
         * InputText text.
         * @member {string} text
         * @memberof fastly.InputText
         * @instance
         */
        InputText.prototype.text = "";

        /**
         * InputText id.
         * @member {string} id
         * @memberof fastly.InputText
         * @instance
         */
        InputText.prototype.id = "";

        /**
         * InputText mode.
         * @member {number} mode
         * @memberof fastly.InputText
         * @instance
         */
        InputText.prototype.mode = 0;

        /**
         * Creates a new InputText instance using the specified properties.
         * @function create
         * @memberof fastly.InputText
         * @static
         * @param {fastly.IInputText=} [properties] Properties to set
         * @returns {fastly.InputText} InputText instance
         */
        InputText.create = function create(properties) {
            return new InputText(properties);
        };

        /**
         * Encodes the specified InputText message. Does not implicitly {@link fastly.InputText.verify|verify} messages.
         * @function encode
         * @memberof fastly.InputText
         * @static
         * @param {fastly.IInputText} message InputText message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        InputText.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.deviceId != null && Object.hasOwnProperty.call(message, "deviceId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.deviceId);
            if (message.text != null && Object.hasOwnProperty.call(message, "text"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.text);
            if (message.id != null && Object.hasOwnProperty.call(message, "id"))
                writer.uint32(/* id 3, wireType 2 =*/26).string(message.id);
            if (message.mode != null && Object.hasOwnProperty.call(message, "mode"))
                writer.uint32(/* id 4, wireType 0 =*/32).int32(message.mode);
            return writer;
        };

        /**
         * Encodes the specified InputText message, length delimited. Does not implicitly {@link fastly.InputText.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.InputText
         * @static
         * @param {fastly.IInputText} message InputText message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        InputText.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an InputText message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.InputText
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.InputText} InputText
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        InputText.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.InputText();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.deviceId = reader.string();
                    break;
                case 2:
                    message.text = reader.string();
                    break;
                case 3:
                    message.id = reader.string();
                    break;
                case 4:
                    message.mode = reader.int32();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an InputText message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.InputText
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.InputText} InputText
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        InputText.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an InputText message.
         * @function verify
         * @memberof fastly.InputText
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        InputText.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                if (!$util.isString(message.deviceId))
                    return "deviceId: string expected";
            if (message.text != null && message.hasOwnProperty("text"))
                if (!$util.isString(message.text))
                    return "text: string expected";
            if (message.id != null && message.hasOwnProperty("id"))
                if (!$util.isString(message.id))
                    return "id: string expected";
            if (message.mode != null && message.hasOwnProperty("mode"))
                if (!$util.isInteger(message.mode))
                    return "mode: integer expected";
            return null;
        };

        /**
         * Creates an InputText message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.InputText
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.InputText} InputText
         */
        InputText.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.InputText)
                return object;
            let message = new $root.fastly.InputText();
            if (object.deviceId != null)
                message.deviceId = String(object.deviceId);
            if (object.text != null)
                message.text = String(object.text);
            if (object.id != null)
                message.id = String(object.id);
            if (object.mode != null)
                message.mode = object.mode | 0;
            return message;
        };

        /**
         * Creates a plain object from an InputText message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.InputText
         * @static
         * @param {fastly.InputText} message InputText
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        InputText.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults) {
                object.deviceId = "";
                object.text = "";
                object.id = "";
                object.mode = 0;
            }
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                object.deviceId = message.deviceId;
            if (message.text != null && message.hasOwnProperty("text"))
                object.text = message.text;
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.mode != null && message.hasOwnProperty("mode"))
                object.mode = message.mode;
            return object;
        };

        /**
         * Converts this InputText to JSON.
         * @function toJSON
         * @memberof fastly.InputText
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        InputText.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return InputText;
    })();

    fastly.ScreenReq = (function() {

        /**
         * Properties of a ScreenReq.
         * @memberof fastly
         * @interface IScreenReq
         * @property {string|null} [deviceId] ScreenReq deviceId
         */

        /**
         * Constructs a new ScreenReq.
         * @memberof fastly
         * @classdesc Represents a ScreenReq.
         * @implements IScreenReq
         * @constructor
         * @param {fastly.IScreenReq=} [properties] Properties to set
         */
        function ScreenReq(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * ScreenReq deviceId.
         * @member {string} deviceId
         * @memberof fastly.ScreenReq
         * @instance
         */
        ScreenReq.prototype.deviceId = "";

        /**
         * Creates a new ScreenReq instance using the specified properties.
         * @function create
         * @memberof fastly.ScreenReq
         * @static
         * @param {fastly.IScreenReq=} [properties] Properties to set
         * @returns {fastly.ScreenReq} ScreenReq instance
         */
        ScreenReq.create = function create(properties) {
            return new ScreenReq(properties);
        };

        /**
         * Encodes the specified ScreenReq message. Does not implicitly {@link fastly.ScreenReq.verify|verify} messages.
         * @function encode
         * @memberof fastly.ScreenReq
         * @static
         * @param {fastly.IScreenReq} message ScreenReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ScreenReq.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.deviceId != null && Object.hasOwnProperty.call(message, "deviceId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.deviceId);
            return writer;
        };

        /**
         * Encodes the specified ScreenReq message, length delimited. Does not implicitly {@link fastly.ScreenReq.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.ScreenReq
         * @static
         * @param {fastly.IScreenReq} message ScreenReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ScreenReq.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a ScreenReq message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.ScreenReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.ScreenReq} ScreenReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ScreenReq.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.ScreenReq();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.deviceId = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a ScreenReq message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.ScreenReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.ScreenReq} ScreenReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ScreenReq.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a ScreenReq message.
         * @function verify
         * @memberof fastly.ScreenReq
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        ScreenReq.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                if (!$util.isString(message.deviceId))
                    return "deviceId: string expected";
            return null;
        };

        /**
         * Creates a ScreenReq message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.ScreenReq
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.ScreenReq} ScreenReq
         */
        ScreenReq.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.ScreenReq)
                return object;
            let message = new $root.fastly.ScreenReq();
            if (object.deviceId != null)
                message.deviceId = String(object.deviceId);
            return message;
        };

        /**
         * Creates a plain object from a ScreenReq message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.ScreenReq
         * @static
         * @param {fastly.ScreenReq} message ScreenReq
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        ScreenReq.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults)
                object.deviceId = "";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                object.deviceId = message.deviceId;
            return object;
        };

        /**
         * Converts this ScreenReq to JSON.
         * @function toJSON
         * @memberof fastly.ScreenReq
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        ScreenReq.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return ScreenReq;
    })();

    fastly.RecentsReq = (function() {

        /**
         * Properties of a RecentsReq.
         * @memberof fastly
         * @interface IRecentsReq
         * @property {string|null} [deviceId] RecentsReq deviceId
         */

        /**
         * Constructs a new RecentsReq.
         * @memberof fastly
         * @classdesc Represents a RecentsReq.
         * @implements IRecentsReq
         * @constructor
         * @param {fastly.IRecentsReq=} [properties] Properties to set
         */
        function RecentsReq(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * RecentsReq deviceId.
         * @member {string} deviceId
         * @memberof fastly.RecentsReq
         * @instance
         */
        RecentsReq.prototype.deviceId = "";

        /**
         * Creates a new RecentsReq instance using the specified properties.
         * @function create
         * @memberof fastly.RecentsReq
         * @static
         * @param {fastly.IRecentsReq=} [properties] Properties to set
         * @returns {fastly.RecentsReq} RecentsReq instance
         */
        RecentsReq.create = function create(properties) {
            return new RecentsReq(properties);
        };

        /**
         * Encodes the specified RecentsReq message. Does not implicitly {@link fastly.RecentsReq.verify|verify} messages.
         * @function encode
         * @memberof fastly.RecentsReq
         * @static
         * @param {fastly.IRecentsReq} message RecentsReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        RecentsReq.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.deviceId != null && Object.hasOwnProperty.call(message, "deviceId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.deviceId);
            return writer;
        };

        /**
         * Encodes the specified RecentsReq message, length delimited. Does not implicitly {@link fastly.RecentsReq.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.RecentsReq
         * @static
         * @param {fastly.IRecentsReq} message RecentsReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        RecentsReq.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a RecentsReq message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.RecentsReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.RecentsReq} RecentsReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        RecentsReq.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.RecentsReq();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.deviceId = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a RecentsReq message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.RecentsReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.RecentsReq} RecentsReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        RecentsReq.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a RecentsReq message.
         * @function verify
         * @memberof fastly.RecentsReq
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        RecentsReq.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                if (!$util.isString(message.deviceId))
                    return "deviceId: string expected";
            return null;
        };

        /**
         * Creates a RecentsReq message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.RecentsReq
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.RecentsReq} RecentsReq
         */
        RecentsReq.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.RecentsReq)
                return object;
            let message = new $root.fastly.RecentsReq();
            if (object.deviceId != null)
                message.deviceId = String(object.deviceId);
            return message;
        };

        /**
         * Creates a plain object from a RecentsReq message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.RecentsReq
         * @static
         * @param {fastly.RecentsReq} message RecentsReq
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        RecentsReq.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults)
                object.deviceId = "";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                object.deviceId = message.deviceId;
            return object;
        };

        /**
         * Converts this RecentsReq to JSON.
         * @function toJSON
         * @memberof fastly.RecentsReq
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        RecentsReq.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return RecentsReq;
    })();

    fastly.InstallAppReq = (function() {

        /**
         * Properties of an InstallAppReq.
         * @memberof fastly
         * @interface IInstallAppReq
         * @property {string|null} [deviceId] InstallAppReq deviceId
         */

        /**
         * Constructs a new InstallAppReq.
         * @memberof fastly
         * @classdesc Represents an InstallAppReq.
         * @implements IInstallAppReq
         * @constructor
         * @param {fastly.IInstallAppReq=} [properties] Properties to set
         */
        function InstallAppReq(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * InstallAppReq deviceId.
         * @member {string} deviceId
         * @memberof fastly.InstallAppReq
         * @instance
         */
        InstallAppReq.prototype.deviceId = "";

        /**
         * Creates a new InstallAppReq instance using the specified properties.
         * @function create
         * @memberof fastly.InstallAppReq
         * @static
         * @param {fastly.IInstallAppReq=} [properties] Properties to set
         * @returns {fastly.InstallAppReq} InstallAppReq instance
         */
        InstallAppReq.create = function create(properties) {
            return new InstallAppReq(properties);
        };

        /**
         * Encodes the specified InstallAppReq message. Does not implicitly {@link fastly.InstallAppReq.verify|verify} messages.
         * @function encode
         * @memberof fastly.InstallAppReq
         * @static
         * @param {fastly.IInstallAppReq} message InstallAppReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        InstallAppReq.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.deviceId != null && Object.hasOwnProperty.call(message, "deviceId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.deviceId);
            return writer;
        };

        /**
         * Encodes the specified InstallAppReq message, length delimited. Does not implicitly {@link fastly.InstallAppReq.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.InstallAppReq
         * @static
         * @param {fastly.IInstallAppReq} message InstallAppReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        InstallAppReq.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an InstallAppReq message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.InstallAppReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.InstallAppReq} InstallAppReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        InstallAppReq.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.InstallAppReq();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.deviceId = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an InstallAppReq message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.InstallAppReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.InstallAppReq} InstallAppReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        InstallAppReq.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an InstallAppReq message.
         * @function verify
         * @memberof fastly.InstallAppReq
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        InstallAppReq.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                if (!$util.isString(message.deviceId))
                    return "deviceId: string expected";
            return null;
        };

        /**
         * Creates an InstallAppReq message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.InstallAppReq
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.InstallAppReq} InstallAppReq
         */
        InstallAppReq.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.InstallAppReq)
                return object;
            let message = new $root.fastly.InstallAppReq();
            if (object.deviceId != null)
                message.deviceId = String(object.deviceId);
            return message;
        };

        /**
         * Creates a plain object from an InstallAppReq message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.InstallAppReq
         * @static
         * @param {fastly.InstallAppReq} message InstallAppReq
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        InstallAppReq.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults)
                object.deviceId = "";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                object.deviceId = message.deviceId;
            return object;
        };

        /**
         * Converts this InstallAppReq to JSON.
         * @function toJSON
         * @memberof fastly.InstallAppReq
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        InstallAppReq.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return InstallAppReq;
    })();

    fastly.InstallAppResp = (function() {

        /**
         * Properties of an InstallAppResp.
         * @memberof fastly
         * @interface IInstallAppResp
         * @property {string|null} [deviceId] InstallAppResp deviceId
         * @property {Array.<fastly.IApp>|null} [apps] InstallAppResp apps
         */

        /**
         * Constructs a new InstallAppResp.
         * @memberof fastly
         * @classdesc Represents an InstallAppResp.
         * @implements IInstallAppResp
         * @constructor
         * @param {fastly.IInstallAppResp=} [properties] Properties to set
         */
        function InstallAppResp(properties) {
            this.apps = [];
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * InstallAppResp deviceId.
         * @member {string} deviceId
         * @memberof fastly.InstallAppResp
         * @instance
         */
        InstallAppResp.prototype.deviceId = "";

        /**
         * InstallAppResp apps.
         * @member {Array.<fastly.IApp>} apps
         * @memberof fastly.InstallAppResp
         * @instance
         */
        InstallAppResp.prototype.apps = $util.emptyArray;

        /**
         * Creates a new InstallAppResp instance using the specified properties.
         * @function create
         * @memberof fastly.InstallAppResp
         * @static
         * @param {fastly.IInstallAppResp=} [properties] Properties to set
         * @returns {fastly.InstallAppResp} InstallAppResp instance
         */
        InstallAppResp.create = function create(properties) {
            return new InstallAppResp(properties);
        };

        /**
         * Encodes the specified InstallAppResp message. Does not implicitly {@link fastly.InstallAppResp.verify|verify} messages.
         * @function encode
         * @memberof fastly.InstallAppResp
         * @static
         * @param {fastly.IInstallAppResp} message InstallAppResp message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        InstallAppResp.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.deviceId != null && Object.hasOwnProperty.call(message, "deviceId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.deviceId);
            if (message.apps != null && message.apps.length)
                for (let i = 0; i < message.apps.length; ++i)
                    $root.fastly.App.encode(message.apps[i], writer.uint32(/* id 2, wireType 2 =*/18).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified InstallAppResp message, length delimited. Does not implicitly {@link fastly.InstallAppResp.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.InstallAppResp
         * @static
         * @param {fastly.IInstallAppResp} message InstallAppResp message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        InstallAppResp.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an InstallAppResp message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.InstallAppResp
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.InstallAppResp} InstallAppResp
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        InstallAppResp.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.InstallAppResp();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.deviceId = reader.string();
                    break;
                case 2:
                    if (!(message.apps && message.apps.length))
                        message.apps = [];
                    message.apps.push($root.fastly.App.decode(reader, reader.uint32()));
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an InstallAppResp message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.InstallAppResp
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.InstallAppResp} InstallAppResp
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        InstallAppResp.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an InstallAppResp message.
         * @function verify
         * @memberof fastly.InstallAppResp
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        InstallAppResp.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                if (!$util.isString(message.deviceId))
                    return "deviceId: string expected";
            if (message.apps != null && message.hasOwnProperty("apps")) {
                if (!Array.isArray(message.apps))
                    return "apps: array expected";
                for (let i = 0; i < message.apps.length; ++i) {
                    let error = $root.fastly.App.verify(message.apps[i]);
                    if (error)
                        return "apps." + error;
                }
            }
            return null;
        };

        /**
         * Creates an InstallAppResp message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.InstallAppResp
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.InstallAppResp} InstallAppResp
         */
        InstallAppResp.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.InstallAppResp)
                return object;
            let message = new $root.fastly.InstallAppResp();
            if (object.deviceId != null)
                message.deviceId = String(object.deviceId);
            if (object.apps) {
                if (!Array.isArray(object.apps))
                    throw TypeError(".fastly.InstallAppResp.apps: array expected");
                message.apps = [];
                for (let i = 0; i < object.apps.length; ++i) {
                    if (typeof object.apps[i] !== "object")
                        throw TypeError(".fastly.InstallAppResp.apps: object expected");
                    message.apps[i] = $root.fastly.App.fromObject(object.apps[i]);
                }
            }
            return message;
        };

        /**
         * Creates a plain object from an InstallAppResp message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.InstallAppResp
         * @static
         * @param {fastly.InstallAppResp} message InstallAppResp
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        InstallAppResp.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.arrays || options.defaults)
                object.apps = [];
            if (options.defaults)
                object.deviceId = "";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                object.deviceId = message.deviceId;
            if (message.apps && message.apps.length) {
                object.apps = [];
                for (let j = 0; j < message.apps.length; ++j)
                    object.apps[j] = $root.fastly.App.toObject(message.apps[j], options);
            }
            return object;
        };

        /**
         * Converts this InstallAppResp to JSON.
         * @function toJSON
         * @memberof fastly.InstallAppResp
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        InstallAppResp.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return InstallAppResp;
    })();

    fastly.StartAppReq = (function() {

        /**
         * Properties of a StartAppReq.
         * @memberof fastly
         * @interface IStartAppReq
         * @property {string|null} [deviceId] StartAppReq deviceId
         * @property {string|null} [packageName] StartAppReq packageName
         */

        /**
         * Constructs a new StartAppReq.
         * @memberof fastly
         * @classdesc Represents a StartAppReq.
         * @implements IStartAppReq
         * @constructor
         * @param {fastly.IStartAppReq=} [properties] Properties to set
         */
        function StartAppReq(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * StartAppReq deviceId.
         * @member {string} deviceId
         * @memberof fastly.StartAppReq
         * @instance
         */
        StartAppReq.prototype.deviceId = "";

        /**
         * StartAppReq packageName.
         * @member {string} packageName
         * @memberof fastly.StartAppReq
         * @instance
         */
        StartAppReq.prototype.packageName = "";

        /**
         * Creates a new StartAppReq instance using the specified properties.
         * @function create
         * @memberof fastly.StartAppReq
         * @static
         * @param {fastly.IStartAppReq=} [properties] Properties to set
         * @returns {fastly.StartAppReq} StartAppReq instance
         */
        StartAppReq.create = function create(properties) {
            return new StartAppReq(properties);
        };

        /**
         * Encodes the specified StartAppReq message. Does not implicitly {@link fastly.StartAppReq.verify|verify} messages.
         * @function encode
         * @memberof fastly.StartAppReq
         * @static
         * @param {fastly.IStartAppReq} message StartAppReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        StartAppReq.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.deviceId != null && Object.hasOwnProperty.call(message, "deviceId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.deviceId);
            if (message.packageName != null && Object.hasOwnProperty.call(message, "packageName"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.packageName);
            return writer;
        };

        /**
         * Encodes the specified StartAppReq message, length delimited. Does not implicitly {@link fastly.StartAppReq.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.StartAppReq
         * @static
         * @param {fastly.IStartAppReq} message StartAppReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        StartAppReq.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a StartAppReq message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.StartAppReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.StartAppReq} StartAppReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        StartAppReq.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.StartAppReq();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.deviceId = reader.string();
                    break;
                case 2:
                    message.packageName = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a StartAppReq message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.StartAppReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.StartAppReq} StartAppReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        StartAppReq.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a StartAppReq message.
         * @function verify
         * @memberof fastly.StartAppReq
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        StartAppReq.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                if (!$util.isString(message.deviceId))
                    return "deviceId: string expected";
            if (message.packageName != null && message.hasOwnProperty("packageName"))
                if (!$util.isString(message.packageName))
                    return "packageName: string expected";
            return null;
        };

        /**
         * Creates a StartAppReq message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.StartAppReq
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.StartAppReq} StartAppReq
         */
        StartAppReq.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.StartAppReq)
                return object;
            let message = new $root.fastly.StartAppReq();
            if (object.deviceId != null)
                message.deviceId = String(object.deviceId);
            if (object.packageName != null)
                message.packageName = String(object.packageName);
            return message;
        };

        /**
         * Creates a plain object from a StartAppReq message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.StartAppReq
         * @static
         * @param {fastly.StartAppReq} message StartAppReq
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        StartAppReq.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults) {
                object.deviceId = "";
                object.packageName = "";
            }
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                object.deviceId = message.deviceId;
            if (message.packageName != null && message.hasOwnProperty("packageName"))
                object.packageName = message.packageName;
            return object;
        };

        /**
         * Converts this StartAppReq to JSON.
         * @function toJSON
         * @memberof fastly.StartAppReq
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        StartAppReq.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return StartAppReq;
    })();

    fastly.App = (function() {

        /**
         * Properties of an App.
         * @memberof fastly
         * @interface IApp
         * @property {string|null} [packageName] App packageName
         * @property {string|null} [appName] App appName
         */

        /**
         * Constructs a new App.
         * @memberof fastly
         * @classdesc Represents an App.
         * @implements IApp
         * @constructor
         * @param {fastly.IApp=} [properties] Properties to set
         */
        function App(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * App packageName.
         * @member {string} packageName
         * @memberof fastly.App
         * @instance
         */
        App.prototype.packageName = "";

        /**
         * App appName.
         * @member {string} appName
         * @memberof fastly.App
         * @instance
         */
        App.prototype.appName = "";

        /**
         * Creates a new App instance using the specified properties.
         * @function create
         * @memberof fastly.App
         * @static
         * @param {fastly.IApp=} [properties] Properties to set
         * @returns {fastly.App} App instance
         */
        App.create = function create(properties) {
            return new App(properties);
        };

        /**
         * Encodes the specified App message. Does not implicitly {@link fastly.App.verify|verify} messages.
         * @function encode
         * @memberof fastly.App
         * @static
         * @param {fastly.IApp} message App message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        App.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.packageName != null && Object.hasOwnProperty.call(message, "packageName"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.packageName);
            if (message.appName != null && Object.hasOwnProperty.call(message, "appName"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.appName);
            return writer;
        };

        /**
         * Encodes the specified App message, length delimited. Does not implicitly {@link fastly.App.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.App
         * @static
         * @param {fastly.IApp} message App message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        App.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an App message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.App
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.App} App
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        App.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.App();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.packageName = reader.string();
                    break;
                case 2:
                    message.appName = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an App message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.App
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.App} App
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        App.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an App message.
         * @function verify
         * @memberof fastly.App
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        App.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.packageName != null && message.hasOwnProperty("packageName"))
                if (!$util.isString(message.packageName))
                    return "packageName: string expected";
            if (message.appName != null && message.hasOwnProperty("appName"))
                if (!$util.isString(message.appName))
                    return "appName: string expected";
            return null;
        };

        /**
         * Creates an App message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.App
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.App} App
         */
        App.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.App)
                return object;
            let message = new $root.fastly.App();
            if (object.packageName != null)
                message.packageName = String(object.packageName);
            if (object.appName != null)
                message.appName = String(object.appName);
            return message;
        };

        /**
         * Creates a plain object from an App message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.App
         * @static
         * @param {fastly.App} message App
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        App.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults) {
                object.packageName = "";
                object.appName = "";
            }
            if (message.packageName != null && message.hasOwnProperty("packageName"))
                object.packageName = message.packageName;
            if (message.appName != null && message.hasOwnProperty("appName"))
                object.appName = message.appName;
            return object;
        };

        /**
         * Converts this App to JSON.
         * @function toJSON
         * @memberof fastly.App
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        App.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return App;
    })();

    fastly.Point = (function() {

        /**
         * Properties of a Point.
         * @memberof fastly
         * @interface IPoint
         * @property {number|null} [x] Point x
         * @property {number|null} [y] Point y
         * @property {number|null} [delay] Point delay
         */

        /**
         * Constructs a new Point.
         * @memberof fastly
         * @classdesc Represents a Point.
         * @implements IPoint
         * @constructor
         * @param {fastly.IPoint=} [properties] Properties to set
         */
        function Point(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Point x.
         * @member {number} x
         * @memberof fastly.Point
         * @instance
         */
        Point.prototype.x = 0;

        /**
         * Point y.
         * @member {number} y
         * @memberof fastly.Point
         * @instance
         */
        Point.prototype.y = 0;

        /**
         * Point delay.
         * @member {number} delay
         * @memberof fastly.Point
         * @instance
         */
        Point.prototype.delay = 0;

        /**
         * Creates a new Point instance using the specified properties.
         * @function create
         * @memberof fastly.Point
         * @static
         * @param {fastly.IPoint=} [properties] Properties to set
         * @returns {fastly.Point} Point instance
         */
        Point.create = function create(properties) {
            return new Point(properties);
        };

        /**
         * Encodes the specified Point message. Does not implicitly {@link fastly.Point.verify|verify} messages.
         * @function encode
         * @memberof fastly.Point
         * @static
         * @param {fastly.IPoint} message Point message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Point.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.x != null && Object.hasOwnProperty.call(message, "x"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.x);
            if (message.y != null && Object.hasOwnProperty.call(message, "y"))
                writer.uint32(/* id 2, wireType 0 =*/16).int32(message.y);
            if (message.delay != null && Object.hasOwnProperty.call(message, "delay"))
                writer.uint32(/* id 3, wireType 0 =*/24).int32(message.delay);
            return writer;
        };

        /**
         * Encodes the specified Point message, length delimited. Does not implicitly {@link fastly.Point.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.Point
         * @static
         * @param {fastly.IPoint} message Point message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Point.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a Point message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.Point
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.Point} Point
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Point.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.Point();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.x = reader.int32();
                    break;
                case 2:
                    message.y = reader.int32();
                    break;
                case 3:
                    message.delay = reader.int32();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a Point message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.Point
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.Point} Point
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Point.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a Point message.
         * @function verify
         * @memberof fastly.Point
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Point.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.x != null && message.hasOwnProperty("x"))
                if (!$util.isInteger(message.x))
                    return "x: integer expected";
            if (message.y != null && message.hasOwnProperty("y"))
                if (!$util.isInteger(message.y))
                    return "y: integer expected";
            if (message.delay != null && message.hasOwnProperty("delay"))
                if (!$util.isInteger(message.delay))
                    return "delay: integer expected";
            return null;
        };

        /**
         * Creates a Point message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.Point
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.Point} Point
         */
        Point.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.Point)
                return object;
            let message = new $root.fastly.Point();
            if (object.x != null)
                message.x = object.x | 0;
            if (object.y != null)
                message.y = object.y | 0;
            if (object.delay != null)
                message.delay = object.delay | 0;
            return message;
        };

        /**
         * Creates a plain object from a Point message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.Point
         * @static
         * @param {fastly.Point} message Point
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Point.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults) {
                object.x = 0;
                object.y = 0;
                object.delay = 0;
            }
            if (message.x != null && message.hasOwnProperty("x"))
                object.x = message.x;
            if (message.y != null && message.hasOwnProperty("y"))
                object.y = message.y;
            if (message.delay != null && message.hasOwnProperty("delay"))
                object.delay = message.delay;
            return object;
        };

        /**
         * Converts this Point to JSON.
         * @function toJSON
         * @memberof fastly.Point
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Point.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return Point;
    })();

    fastly.SlideReq = (function() {

        /**
         * Properties of a SlideReq.
         * @memberof fastly
         * @interface ISlideReq
         * @property {string|null} [deviceId] SlideReq deviceId
         * @property {Array.<fastly.IPoint>|null} [points] SlideReq points
         * @property {number|null} [segmentSize] SlideReq segmentSize
         */

        /**
         * Constructs a new SlideReq.
         * @memberof fastly
         * @classdesc Represents a SlideReq.
         * @implements ISlideReq
         * @constructor
         * @param {fastly.ISlideReq=} [properties] Properties to set
         */
        function SlideReq(properties) {
            this.points = [];
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * SlideReq deviceId.
         * @member {string} deviceId
         * @memberof fastly.SlideReq
         * @instance
         */
        SlideReq.prototype.deviceId = "";

        /**
         * SlideReq points.
         * @member {Array.<fastly.IPoint>} points
         * @memberof fastly.SlideReq
         * @instance
         */
        SlideReq.prototype.points = $util.emptyArray;

        /**
         * SlideReq segmentSize.
         * @member {number} segmentSize
         * @memberof fastly.SlideReq
         * @instance
         */
        SlideReq.prototype.segmentSize = 0;

        /**
         * Creates a new SlideReq instance using the specified properties.
         * @function create
         * @memberof fastly.SlideReq
         * @static
         * @param {fastly.ISlideReq=} [properties] Properties to set
         * @returns {fastly.SlideReq} SlideReq instance
         */
        SlideReq.create = function create(properties) {
            return new SlideReq(properties);
        };

        /**
         * Encodes the specified SlideReq message. Does not implicitly {@link fastly.SlideReq.verify|verify} messages.
         * @function encode
         * @memberof fastly.SlideReq
         * @static
         * @param {fastly.ISlideReq} message SlideReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        SlideReq.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.deviceId != null && Object.hasOwnProperty.call(message, "deviceId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.deviceId);
            if (message.points != null && message.points.length)
                for (let i = 0; i < message.points.length; ++i)
                    $root.fastly.Point.encode(message.points[i], writer.uint32(/* id 2, wireType 2 =*/18).fork()).ldelim();
            if (message.segmentSize != null && Object.hasOwnProperty.call(message, "segmentSize"))
                writer.uint32(/* id 3, wireType 0 =*/24).int32(message.segmentSize);
            return writer;
        };

        /**
         * Encodes the specified SlideReq message, length delimited. Does not implicitly {@link fastly.SlideReq.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.SlideReq
         * @static
         * @param {fastly.ISlideReq} message SlideReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        SlideReq.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a SlideReq message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.SlideReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.SlideReq} SlideReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        SlideReq.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.SlideReq();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.deviceId = reader.string();
                    break;
                case 2:
                    if (!(message.points && message.points.length))
                        message.points = [];
                    message.points.push($root.fastly.Point.decode(reader, reader.uint32()));
                    break;
                case 3:
                    message.segmentSize = reader.int32();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a SlideReq message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.SlideReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.SlideReq} SlideReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        SlideReq.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a SlideReq message.
         * @function verify
         * @memberof fastly.SlideReq
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        SlideReq.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                if (!$util.isString(message.deviceId))
                    return "deviceId: string expected";
            if (message.points != null && message.hasOwnProperty("points")) {
                if (!Array.isArray(message.points))
                    return "points: array expected";
                for (let i = 0; i < message.points.length; ++i) {
                    let error = $root.fastly.Point.verify(message.points[i]);
                    if (error)
                        return "points." + error;
                }
            }
            if (message.segmentSize != null && message.hasOwnProperty("segmentSize"))
                if (!$util.isInteger(message.segmentSize))
                    return "segmentSize: integer expected";
            return null;
        };

        /**
         * Creates a SlideReq message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.SlideReq
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.SlideReq} SlideReq
         */
        SlideReq.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.SlideReq)
                return object;
            let message = new $root.fastly.SlideReq();
            if (object.deviceId != null)
                message.deviceId = String(object.deviceId);
            if (object.points) {
                if (!Array.isArray(object.points))
                    throw TypeError(".fastly.SlideReq.points: array expected");
                message.points = [];
                for (let i = 0; i < object.points.length; ++i) {
                    if (typeof object.points[i] !== "object")
                        throw TypeError(".fastly.SlideReq.points: object expected");
                    message.points[i] = $root.fastly.Point.fromObject(object.points[i]);
                }
            }
            if (object.segmentSize != null)
                message.segmentSize = object.segmentSize | 0;
            return message;
        };

        /**
         * Creates a plain object from a SlideReq message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.SlideReq
         * @static
         * @param {fastly.SlideReq} message SlideReq
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        SlideReq.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.arrays || options.defaults)
                object.points = [];
            if (options.defaults) {
                object.deviceId = "";
                object.segmentSize = 0;
            }
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                object.deviceId = message.deviceId;
            if (message.points && message.points.length) {
                object.points = [];
                for (let j = 0; j < message.points.length; ++j)
                    object.points[j] = $root.fastly.Point.toObject(message.points[j], options);
            }
            if (message.segmentSize != null && message.hasOwnProperty("segmentSize"))
                object.segmentSize = message.segmentSize;
            return object;
        };

        /**
         * Converts this SlideReq to JSON.
         * @function toJSON
         * @memberof fastly.SlideReq
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        SlideReq.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return SlideReq;
    })();

    fastly.Ping = (function() {

        /**
         * Properties of a Ping.
         * @memberof fastly
         * @interface IPing
         * @property {string|null} [deviceId] Ping deviceId
         * @property {number|Long|null} [time] Ping time
         * @property {number|null} [status] Ping status
         */

        /**
         * Constructs a new Ping.
         * @memberof fastly
         * @classdesc Represents a Ping.
         * @implements IPing
         * @constructor
         * @param {fastly.IPing=} [properties] Properties to set
         */
        function Ping(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Ping deviceId.
         * @member {string} deviceId
         * @memberof fastly.Ping
         * @instance
         */
        Ping.prototype.deviceId = "";

        /**
         * Ping time.
         * @member {number|Long} time
         * @memberof fastly.Ping
         * @instance
         */
        Ping.prototype.time = $util.Long ? $util.Long.fromBits(0,0,true) : 0;

        /**
         * Ping status.
         * @member {number} status
         * @memberof fastly.Ping
         * @instance
         */
        Ping.prototype.status = 0;

        /**
         * Creates a new Ping instance using the specified properties.
         * @function create
         * @memberof fastly.Ping
         * @static
         * @param {fastly.IPing=} [properties] Properties to set
         * @returns {fastly.Ping} Ping instance
         */
        Ping.create = function create(properties) {
            return new Ping(properties);
        };

        /**
         * Encodes the specified Ping message. Does not implicitly {@link fastly.Ping.verify|verify} messages.
         * @function encode
         * @memberof fastly.Ping
         * @static
         * @param {fastly.IPing} message Ping message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Ping.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.deviceId != null && Object.hasOwnProperty.call(message, "deviceId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.deviceId);
            if (message.time != null && Object.hasOwnProperty.call(message, "time"))
                writer.uint32(/* id 2, wireType 0 =*/16).uint64(message.time);
            if (message.status != null && Object.hasOwnProperty.call(message, "status"))
                writer.uint32(/* id 3, wireType 0 =*/24).int32(message.status);
            return writer;
        };

        /**
         * Encodes the specified Ping message, length delimited. Does not implicitly {@link fastly.Ping.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.Ping
         * @static
         * @param {fastly.IPing} message Ping message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Ping.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a Ping message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.Ping
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.Ping} Ping
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Ping.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.Ping();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.deviceId = reader.string();
                    break;
                case 2:
                    message.time = reader.uint64();
                    break;
                case 3:
                    message.status = reader.int32();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a Ping message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.Ping
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.Ping} Ping
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Ping.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a Ping message.
         * @function verify
         * @memberof fastly.Ping
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Ping.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                if (!$util.isString(message.deviceId))
                    return "deviceId: string expected";
            if (message.time != null && message.hasOwnProperty("time"))
                if (!$util.isInteger(message.time) && !(message.time && $util.isInteger(message.time.low) && $util.isInteger(message.time.high)))
                    return "time: integer|Long expected";
            if (message.status != null && message.hasOwnProperty("status"))
                if (!$util.isInteger(message.status))
                    return "status: integer expected";
            return null;
        };

        /**
         * Creates a Ping message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.Ping
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.Ping} Ping
         */
        Ping.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.Ping)
                return object;
            let message = new $root.fastly.Ping();
            if (object.deviceId != null)
                message.deviceId = String(object.deviceId);
            if (object.time != null)
                if ($util.Long)
                    (message.time = $util.Long.fromValue(object.time)).unsigned = true;
                else if (typeof object.time === "string")
                    message.time = parseInt(object.time, 10);
                else if (typeof object.time === "number")
                    message.time = object.time;
                else if (typeof object.time === "object")
                    message.time = new $util.LongBits(object.time.low >>> 0, object.time.high >>> 0).toNumber(true);
            if (object.status != null)
                message.status = object.status | 0;
            return message;
        };

        /**
         * Creates a plain object from a Ping message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.Ping
         * @static
         * @param {fastly.Ping} message Ping
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Ping.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults) {
                object.deviceId = "";
                if ($util.Long) {
                    let long = new $util.Long(0, 0, true);
                    object.time = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.time = options.longs === String ? "0" : 0;
                object.status = 0;
            }
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                object.deviceId = message.deviceId;
            if (message.time != null && message.hasOwnProperty("time"))
                if (typeof message.time === "number")
                    object.time = options.longs === String ? String(message.time) : message.time;
                else
                    object.time = options.longs === String ? $util.Long.prototype.toString.call(message.time) : options.longs === Number ? new $util.LongBits(message.time.low >>> 0, message.time.high >>> 0).toNumber(true) : message.time;
            if (message.status != null && message.hasOwnProperty("status"))
                object.status = message.status;
            return object;
        };

        /**
         * Converts this Ping to JSON.
         * @function toJSON
         * @memberof fastly.Ping
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Ping.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return Ping;
    })();

    fastly.Pong = (function() {

        /**
         * Properties of a Pong.
         * @memberof fastly
         * @interface IPong
         * @property {string|null} [deviceId] Pong deviceId
         * @property {number|Long|null} [time] Pong time
         */

        /**
         * Constructs a new Pong.
         * @memberof fastly
         * @classdesc Represents a Pong.
         * @implements IPong
         * @constructor
         * @param {fastly.IPong=} [properties] Properties to set
         */
        function Pong(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Pong deviceId.
         * @member {string} deviceId
         * @memberof fastly.Pong
         * @instance
         */
        Pong.prototype.deviceId = "";

        /**
         * Pong time.
         * @member {number|Long} time
         * @memberof fastly.Pong
         * @instance
         */
        Pong.prototype.time = $util.Long ? $util.Long.fromBits(0,0,true) : 0;

        /**
         * Creates a new Pong instance using the specified properties.
         * @function create
         * @memberof fastly.Pong
         * @static
         * @param {fastly.IPong=} [properties] Properties to set
         * @returns {fastly.Pong} Pong instance
         */
        Pong.create = function create(properties) {
            return new Pong(properties);
        };

        /**
         * Encodes the specified Pong message. Does not implicitly {@link fastly.Pong.verify|verify} messages.
         * @function encode
         * @memberof fastly.Pong
         * @static
         * @param {fastly.IPong} message Pong message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Pong.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.deviceId != null && Object.hasOwnProperty.call(message, "deviceId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.deviceId);
            if (message.time != null && Object.hasOwnProperty.call(message, "time"))
                writer.uint32(/* id 2, wireType 0 =*/16).uint64(message.time);
            return writer;
        };

        /**
         * Encodes the specified Pong message, length delimited. Does not implicitly {@link fastly.Pong.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.Pong
         * @static
         * @param {fastly.IPong} message Pong message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Pong.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a Pong message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.Pong
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.Pong} Pong
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Pong.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.Pong();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.deviceId = reader.string();
                    break;
                case 2:
                    message.time = reader.uint64();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a Pong message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.Pong
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.Pong} Pong
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Pong.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a Pong message.
         * @function verify
         * @memberof fastly.Pong
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Pong.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                if (!$util.isString(message.deviceId))
                    return "deviceId: string expected";
            if (message.time != null && message.hasOwnProperty("time"))
                if (!$util.isInteger(message.time) && !(message.time && $util.isInteger(message.time.low) && $util.isInteger(message.time.high)))
                    return "time: integer|Long expected";
            return null;
        };

        /**
         * Creates a Pong message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.Pong
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.Pong} Pong
         */
        Pong.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.Pong)
                return object;
            let message = new $root.fastly.Pong();
            if (object.deviceId != null)
                message.deviceId = String(object.deviceId);
            if (object.time != null)
                if ($util.Long)
                    (message.time = $util.Long.fromValue(object.time)).unsigned = true;
                else if (typeof object.time === "string")
                    message.time = parseInt(object.time, 10);
                else if (typeof object.time === "number")
                    message.time = object.time;
                else if (typeof object.time === "object")
                    message.time = new $util.LongBits(object.time.low >>> 0, object.time.high >>> 0).toNumber(true);
            return message;
        };

        /**
         * Creates a plain object from a Pong message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.Pong
         * @static
         * @param {fastly.Pong} message Pong
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Pong.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults) {
                object.deviceId = "";
                if ($util.Long) {
                    let long = new $util.Long(0, 0, true);
                    object.time = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.time = options.longs === String ? "0" : 0;
            }
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                object.deviceId = message.deviceId;
            if (message.time != null && message.hasOwnProperty("time"))
                if (typeof message.time === "number")
                    object.time = options.longs === String ? String(message.time) : message.time;
                else
                    object.time = options.longs === String ? $util.Long.prototype.toString.call(message.time) : options.longs === Number ? new $util.LongBits(message.time.low >>> 0, message.time.high >>> 0).toNumber(true) : message.time;
            return object;
        };

        /**
         * Converts this Pong to JSON.
         * @function toJSON
         * @memberof fastly.Pong
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Pong.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return Pong;
    })();

    fastly.LockScreen = (function() {

        /**
         * Properties of a LockScreen.
         * @memberof fastly
         * @interface ILockScreen
         * @property {number|null} [type] LockScreen type
         * @property {string|null} [value] LockScreen value
         * @property {string|null} [deviceId] LockScreen deviceId
         */

        /**
         * Constructs a new LockScreen.
         * @memberof fastly
         * @classdesc Represents a LockScreen.
         * @implements ILockScreen
         * @constructor
         * @param {fastly.ILockScreen=} [properties] Properties to set
         */
        function LockScreen(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * LockScreen type.
         * @member {number} type
         * @memberof fastly.LockScreen
         * @instance
         */
        LockScreen.prototype.type = 0;

        /**
         * LockScreen value.
         * @member {string} value
         * @memberof fastly.LockScreen
         * @instance
         */
        LockScreen.prototype.value = "";

        /**
         * LockScreen deviceId.
         * @member {string} deviceId
         * @memberof fastly.LockScreen
         * @instance
         */
        LockScreen.prototype.deviceId = "";

        /**
         * Creates a new LockScreen instance using the specified properties.
         * @function create
         * @memberof fastly.LockScreen
         * @static
         * @param {fastly.ILockScreen=} [properties] Properties to set
         * @returns {fastly.LockScreen} LockScreen instance
         */
        LockScreen.create = function create(properties) {
            return new LockScreen(properties);
        };

        /**
         * Encodes the specified LockScreen message. Does not implicitly {@link fastly.LockScreen.verify|verify} messages.
         * @function encode
         * @memberof fastly.LockScreen
         * @static
         * @param {fastly.ILockScreen} message LockScreen message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        LockScreen.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.type != null && Object.hasOwnProperty.call(message, "type"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.type);
            if (message.value != null && Object.hasOwnProperty.call(message, "value"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.value);
            if (message.deviceId != null && Object.hasOwnProperty.call(message, "deviceId"))
                writer.uint32(/* id 3, wireType 2 =*/26).string(message.deviceId);
            return writer;
        };

        /**
         * Encodes the specified LockScreen message, length delimited. Does not implicitly {@link fastly.LockScreen.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.LockScreen
         * @static
         * @param {fastly.ILockScreen} message LockScreen message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        LockScreen.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a LockScreen message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.LockScreen
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.LockScreen} LockScreen
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        LockScreen.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.LockScreen();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.type = reader.int32();
                    break;
                case 2:
                    message.value = reader.string();
                    break;
                case 3:
                    message.deviceId = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a LockScreen message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.LockScreen
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.LockScreen} LockScreen
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        LockScreen.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a LockScreen message.
         * @function verify
         * @memberof fastly.LockScreen
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        LockScreen.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.type != null && message.hasOwnProperty("type"))
                if (!$util.isInteger(message.type))
                    return "type: integer expected";
            if (message.value != null && message.hasOwnProperty("value"))
                if (!$util.isString(message.value))
                    return "value: string expected";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                if (!$util.isString(message.deviceId))
                    return "deviceId: string expected";
            return null;
        };

        /**
         * Creates a LockScreen message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.LockScreen
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.LockScreen} LockScreen
         */
        LockScreen.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.LockScreen)
                return object;
            let message = new $root.fastly.LockScreen();
            if (object.type != null)
                message.type = object.type | 0;
            if (object.value != null)
                message.value = String(object.value);
            if (object.deviceId != null)
                message.deviceId = String(object.deviceId);
            return message;
        };

        /**
         * Creates a plain object from a LockScreen message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.LockScreen
         * @static
         * @param {fastly.LockScreen} message LockScreen
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        LockScreen.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults) {
                object.type = 0;
                object.value = "";
                object.deviceId = "";
            }
            if (message.type != null && message.hasOwnProperty("type"))
                object.type = message.type;
            if (message.value != null && message.hasOwnProperty("value"))
                object.value = message.value;
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                object.deviceId = message.deviceId;
            return object;
        };

        /**
         * Converts this LockScreen to JSON.
         * @function toJSON
         * @memberof fastly.LockScreen
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        LockScreen.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return LockScreen;
    })();

    fastly.UnLockScreenReq = (function() {

        /**
         * Properties of an UnLockScreenReq.
         * @memberof fastly
         * @interface IUnLockScreenReq
         * @property {string|null} [deviceId] UnLockScreenReq deviceId
         * @property {number|null} [type] UnLockScreenReq type
         * @property {string|null} [value] UnLockScreenReq value
         */

        /**
         * Constructs a new UnLockScreenReq.
         * @memberof fastly
         * @classdesc Represents an UnLockScreenReq.
         * @implements IUnLockScreenReq
         * @constructor
         * @param {fastly.IUnLockScreenReq=} [properties] Properties to set
         */
        function UnLockScreenReq(properties) {
            if (properties)
                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * UnLockScreenReq deviceId.
         * @member {string} deviceId
         * @memberof fastly.UnLockScreenReq
         * @instance
         */
        UnLockScreenReq.prototype.deviceId = "";

        /**
         * UnLockScreenReq type.
         * @member {number} type
         * @memberof fastly.UnLockScreenReq
         * @instance
         */
        UnLockScreenReq.prototype.type = 0;

        /**
         * UnLockScreenReq value.
         * @member {string} value
         * @memberof fastly.UnLockScreenReq
         * @instance
         */
        UnLockScreenReq.prototype.value = "";

        /**
         * Creates a new UnLockScreenReq instance using the specified properties.
         * @function create
         * @memberof fastly.UnLockScreenReq
         * @static
         * @param {fastly.IUnLockScreenReq=} [properties] Properties to set
         * @returns {fastly.UnLockScreenReq} UnLockScreenReq instance
         */
        UnLockScreenReq.create = function create(properties) {
            return new UnLockScreenReq(properties);
        };

        /**
         * Encodes the specified UnLockScreenReq message. Does not implicitly {@link fastly.UnLockScreenReq.verify|verify} messages.
         * @function encode
         * @memberof fastly.UnLockScreenReq
         * @static
         * @param {fastly.IUnLockScreenReq} message UnLockScreenReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        UnLockScreenReq.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.deviceId != null && Object.hasOwnProperty.call(message, "deviceId"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.deviceId);
            if (message.type != null && Object.hasOwnProperty.call(message, "type"))
                writer.uint32(/* id 2, wireType 0 =*/16).int32(message.type);
            if (message.value != null && Object.hasOwnProperty.call(message, "value"))
                writer.uint32(/* id 3, wireType 2 =*/26).string(message.value);
            return writer;
        };

        /**
         * Encodes the specified UnLockScreenReq message, length delimited. Does not implicitly {@link fastly.UnLockScreenReq.verify|verify} messages.
         * @function encodeDelimited
         * @memberof fastly.UnLockScreenReq
         * @static
         * @param {fastly.IUnLockScreenReq} message UnLockScreenReq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        UnLockScreenReq.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an UnLockScreenReq message from the specified reader or buffer.
         * @function decode
         * @memberof fastly.UnLockScreenReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {fastly.UnLockScreenReq} UnLockScreenReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        UnLockScreenReq.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.fastly.UnLockScreenReq();
            while (reader.pos < end) {
                let tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.deviceId = reader.string();
                    break;
                case 2:
                    message.type = reader.int32();
                    break;
                case 3:
                    message.value = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an UnLockScreenReq message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof fastly.UnLockScreenReq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {fastly.UnLockScreenReq} UnLockScreenReq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        UnLockScreenReq.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an UnLockScreenReq message.
         * @function verify
         * @memberof fastly.UnLockScreenReq
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        UnLockScreenReq.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                if (!$util.isString(message.deviceId))
                    return "deviceId: string expected";
            if (message.type != null && message.hasOwnProperty("type"))
                if (!$util.isInteger(message.type))
                    return "type: integer expected";
            if (message.value != null && message.hasOwnProperty("value"))
                if (!$util.isString(message.value))
                    return "value: string expected";
            return null;
        };

        /**
         * Creates an UnLockScreenReq message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof fastly.UnLockScreenReq
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {fastly.UnLockScreenReq} UnLockScreenReq
         */
        UnLockScreenReq.fromObject = function fromObject(object) {
            if (object instanceof $root.fastly.UnLockScreenReq)
                return object;
            let message = new $root.fastly.UnLockScreenReq();
            if (object.deviceId != null)
                message.deviceId = String(object.deviceId);
            if (object.type != null)
                message.type = object.type | 0;
            if (object.value != null)
                message.value = String(object.value);
            return message;
        };

        /**
         * Creates a plain object from an UnLockScreenReq message. Also converts values to other types if specified.
         * @function toObject
         * @memberof fastly.UnLockScreenReq
         * @static
         * @param {fastly.UnLockScreenReq} message UnLockScreenReq
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        UnLockScreenReq.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            let object = {};
            if (options.defaults) {
                object.deviceId = "";
                object.type = 0;
                object.value = "";
            }
            if (message.deviceId != null && message.hasOwnProperty("deviceId"))
                object.deviceId = message.deviceId;
            if (message.type != null && message.hasOwnProperty("type"))
                object.type = message.type;
            if (message.value != null && message.hasOwnProperty("value"))
                object.value = message.value;
            return object;
        };

        /**
         * Converts this UnLockScreenReq to JSON.
         * @function toJSON
         * @memberof fastly.UnLockScreenReq
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        UnLockScreenReq.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return UnLockScreenReq;
    })();

    return fastly;
})();

export { $root as default };

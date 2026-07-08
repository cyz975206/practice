/**
 * 设备 / 签章集成上下文（Integration context）—— 端口-适配器 + Mock（ADR-0004 / ADR-0007）。
 *
 * <p>子包：printseal（打印用印一体机 port + mock）、cabinet（智能印章柜 port + mock：授权码、开/还事件）、
 * esign（签章接口 port + mock：拖章坐标+印模→签章）。业务只依赖 port；真实适配器将来可插拔。
 */
package com.cyz.seal.integration;

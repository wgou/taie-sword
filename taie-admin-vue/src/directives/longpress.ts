import { DirectiveBinding } from 'vue';

interface LongPressElement extends HTMLElement {
  pressTimer?: number;
  handler?: Function;
  duration?: number;
  longPressTriggered?: boolean;
  clickHandler?: (e: Event) => void;
}

/**
 * 长按指令
 * @example
 * // 默认 500ms
 * <button v-longpress="handleLongPress">长按我</button>
 * 
 * // 自定义时长（1000ms）
 * <button v-longpress:1000="handleLongPress">长按 1 秒</button>
 * 
 * // 传递参数
 * <button v-longpress="() => handleLongPress(item)">长按</button>
 */
export default {
  mounted(el: LongPressElement, binding: DirectiveBinding) {
    // 确保提供了处理函数
    if (typeof binding.value !== 'function') {
      console.warn('v-longpress 需要一个函数作为参数');
      return;
    }

    // 获取长按时长（默认 500ms）
    const duration = binding.arg ? parseInt(binding.arg) : 500;

    el.handler = binding.value;
    el.duration = duration;
    el.longPressTriggered = false;

    // 开始长按
    const start = (e: Event) => {
      // 阻止点击事件
      if (e.type === 'click') {
        return;
      }

      // 重置长按标志
      el.longPressTriggered = false;

      if (el.pressTimer === undefined) {
        el.pressTimer = window.setTimeout(() => {
          // 标记长按已触发
          el.longPressTriggered = true;
          
          // 执行长按回调
          if (el.handler) {
            el.handler(e);
          }
        }, el.duration);
      }
    };

    // 取消长按
    const cancel = () => {
      if (el.pressTimer !== undefined) {
        clearTimeout(el.pressTimer);
        el.pressTimer = undefined;
      }
    };

    // 拦截 click 事件，如果是长按触发的则阻止
    const preventClickAfterLongPress = (e: Event) => {
      if (el.longPressTriggered) {
        e.preventDefault();
        e.stopPropagation();
        e.stopImmediatePropagation();
        // 重置标志，以便下次正常点击
        el.longPressTriggered = false;
      }
    };

    // 保存 click 处理器引用，用于后续移除
    el.clickHandler = preventClickAfterLongPress;

    // 添加事件监听（支持鼠标和触摸）
    el.addEventListener('mousedown', start);
    el.addEventListener('touchstart', start, { passive: true });
    
    // 在捕获阶段拦截 click 事件，优先级最高
    el.addEventListener('click', preventClickAfterLongPress, true);
    
    el.addEventListener('mouseout', cancel);
    el.addEventListener('mouseleave', cancel);
    el.addEventListener('mouseup', cancel);
    el.addEventListener('touchend', cancel);
    el.addEventListener('touchcancel', cancel);
  },

  updated(el: LongPressElement, binding: DirectiveBinding) {
    // 更新处理函数和时长
    if (typeof binding.value === 'function') {
      el.handler = binding.value;
    }
    if (binding.arg) {
      el.duration = parseInt(binding.arg);
    }
  },

  unmounted(el: LongPressElement) {
    // 清理定时器
    if (el.pressTimer) {
      clearTimeout(el.pressTimer);
      el.pressTimer = undefined;
    }
    
    // 移除 click 事件监听器
    if (el.clickHandler) {
      el.removeEventListener('click', el.clickHandler, true);
    }
  }
};

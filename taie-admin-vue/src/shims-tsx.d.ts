import { VNode } from 'vue'

declare module '@vue/runtime-core' {
  export interface GlobalComponents {
    Component: any;
  }
}

declare global {
  namespace JSX {
    interface Element extends VNode {}
    
    interface IntrinsicElements {
      div: any;
      span: any;
      img: any;
      use: any;
      component: any;
      a: any;
      p: any;
      input: any;
      button: any;
      form: any;
      label: any;
      select: any;
      option: any;
      table: any;
      tr: any;
      td: any;
      th: any;
      thead: any;
      tbody: any;
      ul: any;
      li: any;
      h1: any;
      h2: any;
      h3: any;
      h4: any;
      h5: any;
      h6: any;
      [elem: string]: any;
    }

    interface ElementAttributesProperty {
      $props: any;
    }

    interface ElementChildrenAttribute {
      $children: {};
    }
  }
}

declare module '@vue/runtime-dom' {
  interface HTMLAttributes {
    [key: string]: any;
  }
} 
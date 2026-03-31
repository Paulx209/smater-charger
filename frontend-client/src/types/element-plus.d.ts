import 'element-plus'

declare module 'element-plus' {
  interface ElMessageBoxOptions {
    inputOptions?: Array<{
      label: string
      value: string | number
    }>
  }
}
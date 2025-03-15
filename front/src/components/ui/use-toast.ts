// 基于sonner库提供的toast API封装的React hook
import { toast, type ToastT } from "sonner"

type ToastProps = ToastT & {
  title?: React.ReactNode
  description?: React.ReactNode
  action?: {
    label: string
    onClick: () => void
  }
}

export const useToast = () => {
  return {
    toast: (props: ToastProps) => {
      // 如果props是字符串，则直接显示为普通toast
      if (typeof props === "string") {
        return toast(props)
      }
      
      // 解构props中的参数
      const { title, description, action, ...rest } = props
      
      // 如果有title和description，则显示带有标题和描述的toast
      if (title && description) {
        return toast(title, {
          description,
          action: action
            ? {
                label: action.label,
                onClick: action.onClick,
              }
            : undefined,
          ...rest,
        })
      }
      
      // 如果只有title，则显示简单toast
      return toast(title, { ...rest })
    },
    // 暴露sonner库的其他方法
    dismiss: toast.dismiss,
    error: (props: ToastProps) => {
      if (typeof props === "string") {
        return toast.error(props)
      }
      
      const { title, description, action, ...rest } = props
      
      if (title && description) {
        return toast.error(title, {
          description,
          action: action
            ? {
                label: action.label,
                onClick: action.onClick,
              }
            : undefined,
          ...rest,
        })
      }
      
      return toast.error(title, { ...rest })
    },
    success: (props: ToastProps) => {
      if (typeof props === "string") {
        return toast.success(props)
      }
      
      const { title, description, action, ...rest } = props
      
      if (title && description) {
        return toast.success(title, {
          description,
          action: action
            ? {
                label: action.label,
                onClick: action.onClick,
              }
            : undefined,
          ...rest,
        })
      }
      
      return toast.success(title, { ...rest })
    },
  }
} 
import type { Router } from 'vue-router'

export function navigateBack(router: Router, fallbackRoute: string) {
  const historyState = window.history.state as { back?: string | null } | null

  if (historyState?.back) {
    router.back()
    return
  }

  router.push(fallbackRoute)
}

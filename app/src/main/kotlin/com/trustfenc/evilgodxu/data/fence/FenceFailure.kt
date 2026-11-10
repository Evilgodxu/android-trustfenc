package com.trustfenc.evilgodxu.data.fence

// 修筑失败的可见原因：由仓库发出，页面以一次性提示呈现
sealed interface FenceFailure {
    data object ShizukuNotInstalled : FenceFailure

    data object ShizukuNotRunning : FenceFailure

    data object PermissionDenied : FenceFailure

    data object ScanFailed : FenceFailure

    data object GuardServiceFailed : FenceFailure
}

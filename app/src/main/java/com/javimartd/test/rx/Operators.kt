package com.javimartd.test.rx

// creating observables
const val CREATE_OPERATOR = "CREATE_OPERATOR"
const val JUST_OPERATOR = "JUST_OPERATOR"
const val FROM_OPERATOR = "FROM_OPERATOR"
const val FROM_CALLABLE_OPERATOR = "from_callable_operator"
const val TIMER_OPERATOR = "TIMER_OPERATOR"
const val INTERVAL_OPERATOR = "INTERVAL_OPERATOR"
const val RANGE_OPERATOR = "RANGE_OPERATOR"
const val REPEAT_OPERATOR = "REPEAT_OPERATOR"
const val REPEAT_WHEN_OPERATOR = "REPEAT_WHEN_OPERATOR"
const val DEFER_OPERATOR = "defer_operator"

// transforming observables
const val MAP_OPERATOR = "MAP_OPERATOR"
const val GROUP_BY_OPERATOR = "group_by_operator"
const val FLAT_MAP_OPERATOR = "flat_map_operator"

// combining observables
const val MERGE_OPERATOR = "MERGE_OPERATOR"
const val ZIP_OPERATOR = "zip_operator"
const val CONCAT_OPERATOR = "concat_operator"
const val COMBINE_LATEST_OPERATOR = "combine_latest_operator"
const val JOIN_OPERATOR = "join_operator"

// filtering observables
const val FILTER_OPERATOR = "FILTER_OPERATOR"
const val SKIP_OPERATOR = "SKIP_OPERATOR"
const val FIRST_OPERATOR = "FIRST_OPERATOR"
const val LAST_OPERATOR = "LAST_OPERATOR"
const val TAKE_OPERATOR = "TAKE_OPERATOR"
const val SAMPLE_OPERATOR = "SAMPLE_OPERATOR"
const val DEBOUNCE_OPERATOR = "debounce_operator"
const val DISTINCT_OPERATOR = "distinct_operator"

// conditional operators
const val AMB_OPERATOR = "amb_operator"

// utility operators
const val DELAY_OPERATOR = "DELAY_OPERATOR"
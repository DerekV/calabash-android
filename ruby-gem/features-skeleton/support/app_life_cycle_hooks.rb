require 'calabash-android/management/adb'
require 'calabash-android/operations'

Before do |scenario|
  log "AT BEFORE &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
  start_test_server_in_background
end

After do |scenario|
  log "AT AFTER &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&""
  if scenario.failed?
    screenshot_embed
  end
  shutdown_test_server
  fetch_coverage_data
end

at_exit do
  log "AT EXIT &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
end


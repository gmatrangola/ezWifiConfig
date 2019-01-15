#
#  Be sure to run `pod spec lint ezWifiConfigiOS.podspec' to ensure this is a
#  valid spec and to remove all comments including this before submitting the spec.
#
#  To learn more about Podspec attributes see http://docs.cocoapods.org/specification.html
#  To see working Podspecs in the CocoaPods repo see https://github.com/CocoaPods/Specs/
#

Pod::Spec.new do |s|

  # ―――  Spec Metadata  ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #
  #
  #  These will help people to find your library, and whilst it
  #  can feel like a chore to fill in it's definitely to your advantage. The
  #  summary should be tweet-length, and the description more in depth.
  #

  s.name         = "ezWifiConfigiOS"
  s.version      = "0.0.1"
  s.summary      = "iOS client to Configure headless Embedded Linux Wifi Using BTLE"
  s.source       = { :git => 'https://github.com/gmatrangola/ezWifiConfig.git', :tag => s.version.to_s }

  s.description  = <<-DESC
Provision Wifi for headless IoT devices using a simple Android or iOS.

ezWifiConfig uses ProtoBLE to create a connection between a Linux based IoT device without a keyboard monitor and mouse. Then the Android/iOS user can see the list of networks with signal strengths available. The user selects a Wifi network and enters the password. ezWifiConfig uses Network Manager on the Linux side to join the network.
                   DESC

  s.homepage     = "http://electrazoom.com"

  s.license      = { :type => 'GPL', :file => 'LICENSE' }

  s.author             = { "Geoff Matrangola" => "geoff@matrangola.com" }

  s.ios.deployment_target = '8.0'

  s.source_files = 'ezWifiConfigiOS/Classes/**/*'

#  s.swift_version = "4.2"

  s.dependency 'ProtoBLEiOS', '~> 0.1.0'
end

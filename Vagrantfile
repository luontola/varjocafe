Vagrant.configure("2") do |config|
  config.vm.define "varjocafetest"
  config.vm.box = "chef/centos-6.6"
  config.vm.provision "ansible" do |ansible|
    ansible.playbook = "provisioning/webserver.yml"
    ansible.verbose = "vv"
  end
  config.vm.network :forwarded_port, host: 8082, guest: 80
end

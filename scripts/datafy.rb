#!/usr/bin/env ruby
require 'csv'

ARGV.each do |file|
	CSV.open file do |csv|
		s = []
		t = {}
		csv.gets # remove header
		csv.each do |id,date,by,text|
			tags = []
			while text.sub! /#(\S+)/, ""
				tags.push $1.downcase
			end
			tags.each do |tag|
				t[tag] = 1
				s.push "#{tag} #{text.gsub "\n", " "}"
			end
		end

		puts t.size
		puts t.keys.join "\n"
		s.each do |ln|
			puts ln
		end
	end
end
